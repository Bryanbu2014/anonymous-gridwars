package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Simple bot that expands into all directions if there is a cell that does not belong to the bot
 */
public class CheckerboardBot implements PlayerBot {

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();

        double populationSoftLimit;

        //boolean nearby = .getUp(10)

        if(universeView.getCurrentTurn() < 200){
            populationSoftLimit = 4.0;
        } else {
            populationSoftLimit = 2.0;
        }

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);

            if (currentPopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1))) {
                int split = 4;

                boolean NearbyEnemyUp = false;
                boolean NearbyEnemyDown = false;

                for(int i = 0; i < 5; i++) {
                     NearbyEnemyUp = (!universeView.isEmpty(cell.getUp(i))) && (!universeView.belongsToMe(cell.getUp(i)));
                     NearbyEnemyDown = (!universeView.isEmpty(cell.getDown(i))) && (!universeView.belongsToMe(cell.getDown(i)));
                     if (NearbyEnemyUp || NearbyEnemyDown) {
                         break;
                     }
                }

                if (NearbyEnemyUp || NearbyEnemyDown) {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation/split));
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation/split));
                } else {
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, currentPopulation/split));
                    commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, currentPopulation/split));
                    if((currentPopulation > (populationSoftLimit / (universeView.getGrowthRate() - 1)))){
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, currentPopulation/split));
                        commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, currentPopulation/split));
                    }
                }
                /*
                // Check left, right, up, down for cells that don't belong to me
                for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                    if (!universeView.belongsToMe(cell.getNeighbour(direction))) {
                        //split++;
                    }
                }
                */
            }
        }
    }
}
