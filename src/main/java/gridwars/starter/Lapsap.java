package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;
import java.util.ArrayList;
import java.util.List;

public class UnsureV2 implements PlayerBot {
    private Coordinates initialCoordinates;
    private int state = 0;
    private double populationSoftLimit = 0.59;

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();
        if (initialCoordinates == null && !myCells.isEmpty()) {
            initialCoordinates = myCells.get(0);
        }

        if (universeView.getCurrentTurn() > 18) {
            state = 1;
        }

        for (Coordinates cell : myCells) {
            processCell(cell, universeView, commandList);
        }
    }

    private void processCell(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList) {
        int currentPopulation = universeView.getPopulation(cell);
        int usablePopulation = calculateUsablePopulation(currentPopulation, universeView.getCurrentTurn());

        if (state == 0) {
            executeInitialStrategy(cell, usablePopulation, universeView, commandList);
        } else {
            executeExpansionStrategy(cell, usablePopulation, universeView, commandList);
        }
    }

    private int calculateUsablePopulation(int currentPopulation, int currentTurn) {
        return currentPopulation - Math.max(5, (int) Math.sqrt(currentTurn / 4.0));
    }

    private void executeInitialStrategy(Coordinates cell, int usablePopulation, UniverseView universeView,
            List<MovementCommand> commandList) {
        if (universeView.getCurrentTurn() <= 2) {
            distributeEvenly(cell, 20, 25, commandList);
        } else {
            expandTerritory(cell, usablePopulation, universeView, commandList, false);
        }
    }

    private void executeExpansionStrategy(Coordinates cell, int usablePopulation, UniverseView universeView,
            List<MovementCommand> commandList) {
        if (usablePopulation > populationSoftLimit / (universeView.getGrowthRate() - 1)) {
            if (checkAndRespondToNearbyEnemies(cell, usablePopulation, universeView, commandList)) {
                return;
            }
            expandTerritory(cell, usablePopulation, universeView, commandList, true);
        }
    }

    private boolean checkAndRespondToNearbyEnemies(Coordinates cell, int usablePopulation, UniverseView universeView,
            List<MovementCommand> commandList) {
        boolean enemyDetected = false;
        double attackSplit = 2.0;

        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates target = cell.getNeighbour(direction);
            if (!universeView.isEmpty(target) && !universeView.belongsToMe(target)) {
                commandList.add(new MovementCommand(cell, direction, (int) (usablePopulation / attackSplit)));
                enemyDetected = true;
            }
        }

        return enemyDetected;
    }

    private void expandTerritory(Coordinates cell, int usablePopulation, UniverseView universeView,
            List<MovementCommand> commandList, boolean aggressiveExpansion) {
        if (aggressiveExpansion && usablePopulation >= universeView.getMaximumPopulation() / 4.95) {
            distributeEquallyAmongDirections(cell, usablePopulation, commandList);
        } else {
            distributeBasedOnOwnership(cell, usablePopulation, universeView, commandList);
        }
    }

    private void distributeEvenly(Coordinates cell, int upPopulation, int otherPopulation,
            List<MovementCommand> commandList) {
        commandList.add(new MovementCommand(cell, MovementCommand.Direction.UP, upPopulation));
        commandList.add(new MovementCommand(cell, MovementCommand.Direction.DOWN, otherPopulation));
        commandList.add(new MovementCommand(cell, MovementCommand.Direction.LEFT, otherPopulation));
        commandList.add(new MovementCommand(cell, MovementCommand.Direction.RIGHT, otherPopulation));
    }

    private void distributeEquallyAmongDirections(Coordinates cell, int usablePopulation,
            List<MovementCommand> commandList) {
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            commandList.add(new MovementCommand(cell, direction, (usablePopulation - 1) / 4));
        }
    }

    private void distributeBasedOnOwnership(Coordinates cell, int usablePopulation, UniverseView universeView,
            List<MovementCommand> commandList) {
        List<MovementCommand.Direction> ownedDirections = new ArrayList<>();
        List<MovementCommand.Direction> unownedDirections = new ArrayList<>();

        // Identify owned and unowned neighboring cells
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbour = cell.getNeighbour(direction);
            if (universeView.belongsToMe(neighbour)) {
                ownedDirections.add(direction);
            } else if (!universeView.isEmpty(neighbour)) {
                unownedDirections.add(direction);
            }
        }

        // Determine the population to be moved towards each direction
        int totalDirections = ownedDirections.size() + unownedDirections.size();
        if (totalDirections == 0)
            return;

        int populationPerDirection = usablePopulation / totalDirections;
        int remainingPopulation = usablePopulation % totalDirections;

        // Distribute population to owned cells, prioritizing them to strengthen the
        // positions
        for (MovementCommand.Direction direction : ownedDirections) {
            int populationToSend = populationPerDirection + (remainingPopulation > 0 ? 1 : 0);
            if (remainingPopulation > 0)
                remainingPopulation--;
            if (populationToSend > 0) {
                commandList.add(new MovementCommand(cell, direction, populationToSend));
            }
        }

        // Distribute remaining population to unowned cells to expand territory
        for (MovementCommand.Direction direction : unownedDirections) {
            int populationToSend = populationPerDirection + (remainingPopulation > 0 ? 1 : 0);
            if (remainingPopulation > 0)
                remainingPopulation--;
            if (populationToSend > 0) {
                commandList.add(new MovementCommand(cell, direction, populationToSend));
            }
        }
    }

}
