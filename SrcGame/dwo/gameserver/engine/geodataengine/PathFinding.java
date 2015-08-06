package dwo.gameserver.engine.geodataengine;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.pathfinding.PathComputeBuffer;
import dwo.gameserver.engine.geodataengine.pathfinding.PathNode;
import dwo.gameserver.engine.geodataengine.pathfinding.PathNodeList;
import dwo.gameserver.engine.geodataengine.pathfinding.PathNodePositionSet;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;

/**
 * @author Forsaiken
 */

public class PathFinding
{
	public static final int MODE_MASK_PC = 1;
	public static final int MODE_MASK_NPC = 1 << 1;

	public static final float STRAIGHT_MOVE_COST = 1.0F;
	public static final float STRAIGHT_MOVE_COST_CLOSE_TO_WALL = 2.0F;
	public static final float DIAGONAL_MOVE_COST_CLOSE_TO_WALL = STRAIGHT_MOVE_COST_CLOSE_TO_WALL + 1.4142135623730951f;
	public static final float DIAGONAL_MOVE_COST = 1.4142135623730951f;
	public static final byte DIRECTION_NONE = 0;

	public static final byte DIRECTION_X_POS = 1;
	public static final byte DIRECTION_X_NEG = 2;
	public static final byte DIRECTION_Y_POS = 3;
	public static final byte DIRECTION_Y_NEG = 4;

	public static final byte DIRECTION_X_POS_Y_POS = 5;
	public static final byte DIRECTION_X_POS_Y_NEG = 6;
	public static final byte DIRECTION_X_NEG_Y_POS = 7;
	public static final byte DIRECTION_X_NEG_Y_NEG = 8;
	public static PathFinding _instance;
	private final ThreadLocal<PathComputeBuffer> _pathComputeBuffer;
	int _allocatedBuffers;
	private long _totalComputingTimeNanosFound;
	private long _totalComputingTimeNanosFoundBuild;
	private long _totalComputingTimeNanosNotFound;
	private int _pathsComputedFound;
	private int _pathsComputedNotFound;

	private PathFinding()
	{
		_pathComputeBuffer = new ThreadLocal<PathComputeBuffer>()
		{
			@Override
			protected PathComputeBuffer initialValue()
			{
				_allocatedBuffers++;
				return new PathComputeBuffer(Config.GEODATA_PATHFINDING_ALLOW_DIAGONAL_MOVEMENT, Config.GEODATA_PATHFINDING_COMPUTE_BUFFER_CAPACITY);
			}
		};
	}

	public static void attachNeighbors(PathNode node, PathComputeBuffer pathComputeBuffer)
	{
		int geoX = node.getGeoX();
		int geoY = node.getGeoY();

		short heightAndNSWE = node.getHeightAndNSWE();

		float moveCostStraight = node.getCost() + STRAIGHT_MOVE_COST;
		float moveCostStraightCloseToWall = node.getCost() + STRAIGHT_MOVE_COST_CLOSE_TO_WALL;

		float moveCostDiagonal = node.getCost() + DIAGONAL_MOVE_COST;
		float moveCostDiagonalCloseToWall = node.getCost() + DIAGONAL_MOVE_COST_CLOSE_TO_WALL;

		switch(node.getDirection())
		{
			case DIRECTION_NONE:
				if(GeoEngine.checkEAST(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
				}

				if(GeoEngine.checkWEST(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
				}

				if(GeoEngine.checkSOUTH(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));
				}

				if(GeoEngine.checkNORTH(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));
				}

				if(pathComputeBuffer.allowDiagonalMovement())
				{
					if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
					}

					if(GeoEngine.checkNORTHEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
					}

					if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
					}

					if(GeoEngine.checkNORTHWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
					}
				}
				break;

			case DIRECTION_X_POS:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
					}
				}
				else
				{
					if(GeoEngine.checkEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkSOUTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkNORTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonalCloseToWall));
						}

						if(GeoEngine.checkNORTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonalCloseToWall));
						}
					}
				}
				break;

			case DIRECTION_X_NEG:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
					}
				}
				else
				{
					if(GeoEngine.checkWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkSOUTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkNORTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonalCloseToWall));
						}

						if(GeoEngine.checkNORTHWEST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonalCloseToWall));
						}
					}
				}
				break;

			case DIRECTION_Y_POS:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
					}
				}
				else
				{
					if(GeoEngine.checkEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkSOUTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonalCloseToWall));
						}

						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonalCloseToWall));
						}
					}
				}
				break;

			case DIRECTION_Y_NEG:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
					}
				}
				else
				{
					if(GeoEngine.checkEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkNORTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkNORTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonalCloseToWall));
						}

						if(GeoEngine.checkNORTHWEST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonalCloseToWall));
						}
					}
				}
				break;

			case DIRECTION_X_POS_Y_POS:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));

					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
				}
				else
				{
					if(GeoEngine.checkEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkSOUTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
						}

						if(GeoEngine.checkNORTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
						}

						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
						}
					}
				}
				break;

			case DIRECTION_X_POS_Y_NEG:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));

					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
				}
				else
				{
					if(GeoEngine.checkEAST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY, heightAndNSWE, DIRECTION_X_POS, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkNORTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
						}

						if(GeoEngine.checkNORTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
						}

						if(GeoEngine.checkNORTHWEST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
						}
					}
				}
				break;

			case DIRECTION_X_NEG_Y_POS:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraight));

					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
				}
				else
				{
					if(GeoEngine.checkWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkSOUTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY + 1, heightAndNSWE, DIRECTION_Y_POS, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY + 1, heightAndNSWE, DIRECTION_X_POS_Y_POS, moveCostDiagonal));
						}

						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
						}

						if(GeoEngine.checkNORTHWEST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
						}
					}
				}
				break;

			case DIRECTION_X_NEG_Y_NEG:
				if(GeoEngine.checkNSWEALL(heightAndNSWE))
				{
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraight));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraight));

					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
					node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
				}
				else
				{
					if(GeoEngine.checkWEST(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY, heightAndNSWE, DIRECTION_X_NEG, moveCostStraightCloseToWall));
					}

					if(GeoEngine.checkNORTH(heightAndNSWE))
					{
						node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX, geoY - 1, heightAndNSWE, DIRECTION_Y_NEG, moveCostStraightCloseToWall));
					}

					if(pathComputeBuffer.allowDiagonalMovement())
					{
						if(GeoEngine.checkNORTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX + 1, geoY - 1, heightAndNSWE, DIRECTION_X_POS_Y_NEG, moveCostDiagonal));
						}

						if(GeoEngine.checkSOUTHEAST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY + 1, heightAndNSWE, DIRECTION_X_NEG_Y_POS, moveCostDiagonal));
						}

						if(GeoEngine.checkNORTHWEST(heightAndNSWE))
						{
							node.attachNeighbor(pathComputeBuffer.getQueuedPathNode(geoX - 1, geoY - 1, heightAndNSWE, DIRECTION_X_NEG_Y_NEG, moveCostDiagonal));
						}
					}
				}
				break;

			default:
				throw new RuntimeException();
		}
	}

	public static void init()
	{
		_instance = new PathFinding();
	}

	public static PathFinding getInstance()
	{
		return _instance;
	}

	public static boolean pathFindingEnabledFor(L2Object object)
	{
		return object instanceof L2PcInstance ? (Config.GEODATA_PATHFINDING_MODE & MODE_MASK_PC) != 0 : (Config.GEODATA_PATHFINDING_MODE & MODE_MASK_NPC) != 0;
	}

	private PathComputeBuffer getQueuedComputeBuffer()
	{
		return _pathComputeBuffer.get();
	}

	public int getPathsComputedFound()
	{
		return _pathsComputedFound;
	}

	public int getPathsComputedNotFound()
	{
		return _pathsComputedNotFound;
	}

	public long getTotalComputingTimeNanosFound()
	{
		return _totalComputingTimeNanosFound;
	}

	public long getTotalComputingTimeNanosFoundBuild()
	{
		return _totalComputingTimeNanosFoundBuild;
	}

	public long getTotalComputingTimeNanosNotFound()
	{
		return _totalComputingTimeNanosNotFound;
	}

	public int getAllocatedBuffers()
	{
		return _allocatedBuffers;
	}

	public void resetStats()
	{
		_pathsComputedFound = 0;
		_pathsComputedNotFound = 0;
		_totalComputingTimeNanosFound = 0;
		_totalComputingTimeNanosFoundBuild = 0;
		_totalComputingTimeNanosNotFound = 0;
		_allocatedBuffers = 0;
	}

	public void deleteBuffers()
	{

	}

	public Location[] findPath(int x1, int y1, int z1, int x2, int y2, int z2, boolean playerMove, int instanceId)
	{
		if(Config.GEODATA_PATHFINDING_MODE <= 0)
		{
			return computeNotFound(x1, y1, z1, x2, y2, z2, instanceId);
		}

		int geoX1 = GeoEngine.getGeoX(x1);
		int geoY1 = GeoEngine.getGeoY(y1);
		short heightAndNSWE1 = GeoEngine.getInstance().nGetHeightAndNSWE(geoX1, geoY1, z1);

		int geoX2 = GeoEngine.getGeoX(x2);
		int geoY2 = GeoEngine.getGeoY(y2);
		short heightAndNSWE2 = GeoEngine.getInstance().nGetHeightAndNSWE(geoX2, geoY2, z2);
		int height2 = GeoEngine.getHeight(heightAndNSWE2);

		PathComputeBuffer pathComputeBuffer = getQueuedComputeBuffer();
		long pathComputeStartTime = System.nanoTime();

		try
		{
			PathNodeList open = pathComputeBuffer.getOpen();
			PathNodePositionSet closed = pathComputeBuffer.getClosed();

			// not reachable :x (on 4k buffers limit is 4k world units)
			if(!closed.init(geoX1, geoY1, geoX2, geoY2))
			{
				return computeNotFound(x1, y1, z1, x2, y2, z2, instanceId);
			}

			PathNode startNode = pathComputeBuffer.getStartNode(geoX1, geoY1, heightAndNSWE1);
			PathNode endNode = pathComputeBuffer.getEndNode(geoX2, geoY2, heightAndNSWE2);

			open.addLastUnsafe(startNode);

			PathNode node;
			PathNode neighbor;
			PathNode[] neighbors;
			PathNode queued;
			float heuristic;
			int shiftIndex;
			int shiftInsertIndex;
			int i;
			int diffX;
			int diffY;
			int diffZ;

			while(pathComputeBuffer.hasRemainingNodes() && !open.isEmpty())
			{
				node = open.getLowestCost();

				// Have we reached our target?
				if(node.getGeoX() == endNode.getGeoX() &&
					node.getGeoY() == endNode.getGeoY() &&
					node.getHeightAndNSWE() == endNode.getHeightAndNSWE())
				{
					_totalComputingTimeNanosFound += System.nanoTime() - pathComputeStartTime;

					long pathOptimizeStartTime = System.nanoTime();
					Location[] path = pathComputeBuffer.buildPath(node, x1, y1, z1, x2, y2, z2, playerMove, instanceId);

					_totalComputingTimeNanosFoundBuild += System.nanoTime() - pathOptimizeStartTime;
					_pathsComputedFound++;
					return path;
				}

				// Get the node with the lowest costs+heuristic
				open.removeLowestCost();

				if(!open.isEmpty() && open.getLowestCost().equals(node))
				{
					throw new RuntimeException();
				}

				// compute it`s neighbors
				attachNeighbors(node, pathComputeBuffer);
				neighbors = node.getNeighbors();

				for(i = node.getNeighborsSize(); i-- > 0; )
				{
					neighbor = neighbors[i];

					// check if the queue already contains a node at neighbor`s location
					// if not, it get inserted into the openLoc
					queued = closed.addIfAbsentHeightUnknown(neighbor, instanceId);
					if(queued.equals(PathNodePositionSet.STATIC_OBJECT_NOT_FOUND))
					{
						// no it did not, so we add the neighbor now
						neighbor.setParent(node);

						diffX = geoX2 - neighbor.getGeoX();
						diffY = geoY2 - neighbor.getGeoY();
						diffZ = height2 - neighbor.getHeight();
						heuristic = (float) (Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ) * Config.GEODATA_PATHFINDING_HEURISTIC_MOD_PC);

						neighbor.setHeuristic(heuristic);
						open.addCostSorted(neighbor, 0);
					}
					else if(queued.equals(PathNodePositionSet.STATIC_OBJECT_OUT_OF_GRID))
					{
						// neighbor comes back to queue
						pathComputeBuffer.queueNode(neighbor);
					}
					else
					{
						// neighbor comes back to queue
						pathComputeBuffer.queueNode(neighbor);

						// continue if the queued node is already computed
						if(queued.isComputed())
						{
							continue;
						}

						// continue if the queued node cost is lower or equal
						if(queued.getCost() <= neighbor.getCost())
						{
							continue;
						}

						// get the current position in queue
						shiftIndex = open.indexOf(queued);

						// get the new position in the queue with the given cost
						shiftInsertIndex = open.binarySearchLowestCost(shiftIndex, neighbor.getCost() + queued.getHeuristic());
						if(shiftInsertIndex < 0)
						{
							shiftInsertIndex = -shiftInsertIndex - 2;
						}
						else
						{
							shiftInsertIndex--;
						}

						// set the new cost and the parent
						// heuristic did not changed because the node was not moved :)
						queued.setParent(node);
						queued.setCost(neighbor.getCost());

						// finally shift if needed
						if(shiftInsertIndex > shiftIndex)
						{
							open.shiftInsert(shiftIndex, shiftInsertIndex, queued);
						}
					}
				}

				node.setComputed();
			}

			_totalComputingTimeNanosNotFound += System.nanoTime() - pathComputeStartTime;
			_pathsComputedNotFound++;

			return computeNotFound(x1, y1, z1, x2, y2, z2, instanceId);
		}
		catch(Exception e)
		{
			return computeNotFound(x1, y1, z1, x2, y2, z2, instanceId);
		}
		finally
		{
			pathComputeBuffer.reset();
		}
	}

	private Location[] computeNotFound(int x1, int y1, int z1, int x2, int y2, int z2, int instanceId)
	{
		if(!Config.GEODATA_PATHFINDING_STOP_IF_NO_PATH_FOUND_PC)
		{
			return new Location[]{
				new Location(x1, y1, z1), GeoEngine.getInstance().moveCheck(x1, y1, z1, x2, y2, z2, instanceId)
			};
		}
		return Location.EMPTY_LOCATION;
	}
}