package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.floor;


/**
 * Simple bot that expands into all directions if there is a cell that does not belong to the bot
 */

public class CheckerboardBotV4 implements PlayerBot {
    Coordinates initialCoordinates;

    int state = 0;

    //private BotState state = new ReproduceState();

    //public void previousState();
    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();
        if ((universeView.getCurrentTurn() == 1) || (universeView.getCurrentTurn() == 2)) {
            this.initialCoordinates = myCells.get(0);
        }

        double populationSoftLimit;
        // Soft limit ensure that we get maximum benefit from the growth rate
        populationSoftLimit = 0.600625; //0.600625

        if (universeView.getCurrentTurn() > 60) {
            this.state = 1;
            //populationSoftLimit = 0.600625;
        }

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);
            // splits population into 4 equal parts
            int split = 4;
            int usablePopulation = currentPopulation - 4;
            boolean NearbyEnemyUp = false;
            boolean NearbyEnemyDown = false;
            boolean NearbyEnemyLeft = false;
            boolean NearbyEnemyRight = false;
            boolean Outer = false;
            boolean Inner = false;
            boolean corner = false;

            boolean FacingRightUp = false;
            boolean FacingRightDown = false;
            boolean FacingLeftUp = false;
            boolean FacingLeftDown = false;
            boolean center = false;
            boolean OnTheLeft = false;
            boolean OnTheRight = false;
            boolean OnTop = false;
            boolean OnBottom = false;



            double attackSplit = 1.79;

            Random rand = new Random();
            int rand_direction = rand.nextInt(4);

            List< MovementCommand.Direction> outerDirections = new ArrayList<>();
            List< MovementCommand.Direction> innerDirections = new ArrayList<>();
            int outerSplit = 0;
            int innerSplit = 0;


            if ((cell.getX() == initialCoordinates.getX()) && (cell.getY() == initialCoordinates.getY())) {
                center = true;
            }else if ((cell.getX() > initialCoordinates.getX()) && (cell.getY() > initialCoordinates.getY())){
                FacingRightDown = true;
            } else if ((cell.getX() > initialCoordinates.getX()) && (cell.getY() < initialCoordinates.getY())){
                FacingRightUp = true;
            } else if ((cell.getX() < initialCoordinates.getX()) && (cell.getY() > initialCoordinates.getY())){
                FacingLeftDown = true;
            } else if ((cell.getX() < initialCoordinates.getX()) && (cell.getY() < initialCoordinates.getY())){
                FacingLeftUp = true;
            } else if ((cell.getX() == initialCoordinates.getX()) && (cell.getY() < initialCoordinates.getY())) {
                OnTop = true;
            } else if ((cell.getX() == initialCoordinates.getX()) && (cell.getY() > initialCoordinates.getY())) {
                OnBottom = true;
            } else if ((cell.getX() > initialCoordinates.getX()) && (cell.getY() == initialCoordinates.getY())) {
                OnTheRight = true;
            } else if ((cell.getX() < initialCoordinates.getX()) && (cell.getY() == initialCoordinates.getY())) {
                OnTheLeft = true;
            }
            if (this.state == 1){


                // if the population of the current box exceeds 20 (2.0/0.1), we enter a phase that splits our population to expand the territory
                if (usablePopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1))) {
                    // this loop looks for enemies within a certain distance, if an enemy box is found, it breaks out of the loop.
                    for (int i = 0; i < (universeView.getUniverseSize()); i++) {
                        NearbyEnemyUp = (!universeView.isEmpty(cell.getUp(i))) && (!universeView.belongsToMe(cell.getUp(i)));
                        NearbyEnemyDown = (!universeView.isEmpty(cell.getDown(i))) && (!universeView.belongsToMe(cell.getDown(i)));
                        NearbyEnemyLeft = (!universeView.isEmpty(cell.getLeft(i))) && (!universeView.belongsToMe(cell.getLeft(i)));
                        NearbyEnemyRight = (!universeView.isEmpty(cell.getRight(i))) && (!universeView.belongsToMe(cell.getRight(i)));
                        if (NearbyEnemyUp || NearbyEnemyDown || NearbyEnemyLeft || NearbyEnemyRight) {
                            break;
                        }
                    }

                    // if there is enemy nearby, the boxes closets to the enemy will expand towards the enemy to attack them
                    if (NearbyEnemyUp) {
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, (int) (usablePopulation / attackSplit)));
                        usablePopulation = usablePopulation - (int) (usablePopulation / attackSplit);
                    } else if (NearbyEnemyDown) {
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, (int) (usablePopulation / attackSplit)));
                        usablePopulation = usablePopulation - (int) (usablePopulation / attackSplit);
                    } else if (NearbyEnemyRight) {
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, (int) (usablePopulation / attackSplit)));
                        usablePopulation = usablePopulation - (int) (usablePopulation / attackSplit);
                    } else if (NearbyEnemyLeft) {
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, (int) (usablePopulation / attackSplit)));
                        usablePopulation = usablePopulation - (int) (usablePopulation / attackSplit);
                    }
                    // if out population has hit the limit, split into 5 equal parts, so we can solidify the center

                    if (usablePopulation >= (universeView.getMaximumPopulation()/4.5)){

                        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                            commandList.add(new MovementCommand(cell, direction, (usablePopulation - 3) / 4));
                        }
                        //commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, 47));
                        //commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, 47));
                    } else {

                        // Check left, right, up, down for cells that don't belong to me
                        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                            if (!universeView.belongsToMe(cell.getNeighbour(direction))) {
                                outerSplit++;
                                outerDirections.add(direction);
                                ;
                            } else if ((universeView.belongsToMe(cell.getNeighbour(direction)))) {
                                innerDirections.add(direction);
                                innerSplit++;
                            }

                        }

                        if (innerSplit == outerSplit) {
                            corner = true;
                        } else if (innerSplit > outerSplit) {
                            Inner = true;
                        } else {
                            Outer = true;
                        }
                        // Expand
                        if (Inner) {
                            if (FacingLeftUp) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 2));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 2));
                            } else if (FacingLeftDown) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 2));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 2));
                            } else if (FacingRightUp) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 2));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 2));
                            } else if (FacingRightDown) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 2));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 2));
                            } else if (center) {
                                for (MovementCommand.Direction direction : innerDirections) {
                                    commandList.add(new MovementCommand(cell, direction, usablePopulation / innerSplit));
                                }
                            } else if (OnTop) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 3));
                            } else if (OnBottom) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation /3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 3));
                            } else if (OnTheLeft) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation /3));
                            } else if (OnTheRight) {
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation /3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 3));
                                commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 3));
                            }
                        } else if (Outer){
                            for (MovementCommand.Direction direction : outerDirections) {
                                commandList.add(new MovementCommand(cell, direction, usablePopulation / outerSplit));
                            }
                        } else if (corner){
                            for (MovementCommand.Direction direction : outerDirections) {
                                commandList.add(new MovementCommand(cell, direction, usablePopulation / 2));
                            }
                        }


                    }
                }
            } else {
                if (usablePopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1))) {
                    // Check left, right, up, down for cells that don't belong to me
                    for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                        if (!universeView.belongsToMe(cell.getNeighbour(direction))) {
                            outerSplit++;
                            outerDirections.add(direction);
                            ;
                        } else if ((universeView.belongsToMe(cell.getNeighbour(direction)))) {
                            innerDirections.add(direction);
                            innerSplit++;
                        }

                    }

                    if (innerSplit == outerSplit) {
                        corner = true;
                    } else if (innerSplit > outerSplit) {
                        Inner = true;
                    } else {
                        Outer = true;
                    }
                    // Expand
                    if (Inner) {
                        if (FacingLeftUp) {
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 2));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 2));
                        } else if (FacingLeftDown) {
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 2));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 2));
                        } else if (FacingRightUp) {
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 2));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 2));
                        } else if (FacingRightDown) {
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 2));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 2));
                        } else if (center) {
                            for (MovementCommand.Direction direction : innerDirections) {
                                commandList.add(new MovementCommand(cell, direction, usablePopulation / innerSplit));
                            }
                        } else if (OnTop) {
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 3));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation ));
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 3));
                        } else if (OnBottom) {
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation / 3));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation ));
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation / 3));
                        } else if (OnTheLeft) {
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 3));
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 3));
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, usablePopulation ));
                        } else if (OnTheRight) {
                            commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, usablePopulation ));
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, usablePopulation / 3));
                            //commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, usablePopulation / 3));
                        }
                    } else if (Outer) {
                        for (MovementCommand.Direction direction : outerDirections) {
                            commandList.add(new MovementCommand(cell, direction, usablePopulation / outerSplit));
                        }
                    } else if (corner) {
                        for (MovementCommand.Direction direction : outerDirections) {
                            commandList.add(new MovementCommand(cell, direction, usablePopulation / 2));
                        }
                    } else {
                        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                            commandList.add(new MovementCommand(cell, direction, usablePopulation / 4));
                        }
                    }
                }

            }
        }

    }
}
