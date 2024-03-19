package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Simple bot that expands into all directions if there is a cell that does not belong to the bot
 */
public class CheckerboardBotV2 implements PlayerBot {

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();

        double populationSoftLimit;

        populationSoftLimit = 2.0;

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);

            if (currentPopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1))) {
                int split = 4;

                boolean NearbyEnemyUp = false;
                boolean NearbyEnemyDown = false;
                boolean NearbyEnemyLeft = false;
                boolean NearbyEnemyRight = false;

                for(int i = 0; i < 5; i++) {
                     NearbyEnemyUp = (!universeView.isEmpty(cell.getUp(i))) && (!universeView.belongsToMe(cell.getUp(i)));
                     NearbyEnemyDown = (!universeView.isEmpty(cell.getDown(i))) && (!universeView.belongsToMe(cell.getDown(i)));
                     NearbyEnemyLeft = (!universeView.isEmpty(cell.getLeft(i))) && (!universeView.belongsToMe(cell.getLeft(i)));
                     NearbyEnemyRight = (!universeView.isEmpty(cell.getRight(i))) && (!universeView.belongsToMe(cell.getRight(i)));
                     if (NearbyEnemyUp || NearbyEnemyDown || NearbyEnemyLeft || NearbyEnemyRight) {
                         break;
                     }
                }

                if (NearbyEnemyUp) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation/2));
                } else if (NearbyEnemyDown) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation/2));
                } else if (NearbyEnemyRight) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, currentPopulation/2));
                } else if (NearbyEnemyLeft) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, currentPopulation/2));
                } else {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, currentPopulation/split));
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, currentPopulation/split));
                    if((currentPopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1)))){
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation/split));
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation/split));
                    }
                }
            }
        }
    }
}
