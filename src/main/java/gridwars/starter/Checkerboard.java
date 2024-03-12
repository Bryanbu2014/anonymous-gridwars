package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Simple bot that expands into all directions if there is a cell that does not belong to the bot
 */
public class Checkerboard implements PlayerBot {

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();

        double populationSoftLimit;

        // Soft limit ensure that we get maximum benefit from the growth rate
        populationSoftLimit = 2.0;

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);
            // splits population into 4 equal parts
            int split = 4;

            // if the population of the current box exceeds 20 (2.0/0.1), we enter a phase that splits our population to expand the territory
            if (currentPopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1))) {
                boolean NearbyEnemyUp = false;
                boolean NearbyEnemyDown = false;
                boolean NearbyEnemyLeft = false;
                boolean NearbyEnemyRight = false;

                // this loop looks for enemies within a certain distance, if an enemy box is found, it breaks out of the loop.
                for (int i = 0; i < 11; i++) {
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
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation / 2));
                } else if (NearbyEnemyDown) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation / 2));
                } else if (NearbyEnemyRight) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, currentPopulation / 2));
                } else if (NearbyEnemyLeft) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, currentPopulation / 2));
                } else {
                    // if out population has hit the limit, split into 5 equal parts so we can solidify the center
                    if (currentPopulation == universeView.getMaximumPopulation()){
                        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                            commandList.add(new MovementCommand(cell, direction, currentPopulation / 5));
                        }
                    } else {
                        // if out population is not at the limit, split into 4 equal parts to expand aggressively.
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, currentPopulation / split));
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, currentPopulation / split));
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation / split));
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation / split));
                    }

                }
            }
        }
    }
}
