package gridwars.starter;

import cern.ais.gridwars.Emulator;

/**
 * Instantiates the example bots and starts the game emulator.
 */
public class EmulatorRunner {

    public static void main(String[] args) {
        // MovingBot blueBot = new MovingBot();
        // ExpandBot blueBot = new ExpandBot();

        // TestBot blueBot = new TestBot();
        // CheckerboardBotV2 redBot = new CheckerboardBotV2();
        // CheckerboardBotV3 blueBot = new CheckerboardBotV3();
        // CheckerboardBotV3 redBot = new CheckerboardBotV3();
        UnsureV2 blueBot = new UnsureV2();
        Unsure redBot = new Unsure();

        Emulator.playMatch(blueBot, redBot);
    }
}
