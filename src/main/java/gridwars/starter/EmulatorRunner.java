package gridwars.starter;

import cern.ais.gridwars.Emulator;


/**
 * Instantiates the example bots and starts the game emulator.
 */
public class EmulatorRunner {

    public static void main(String[] args) {
        //MovingBot blueBot = new MovingBot();
        //ExpandBot blueBot = new ExpandBot();

        //TestBot blueBot = new TestBot();
        //CheckerboardBotV2 redBot = new CheckerboardBotV2();
        //CheckerboardBotV3 blueBot = new CheckerboardBotV3();
        //CheckerboardBotV3 redBot = new CheckerboardBotV3();
        CheckerboardBotV4 redBot = new CheckerboardBotV4();
        Checkerboard blueBot = new Checkerboard();

        Emulator.playMatch(blueBot, redBot);
    }
}
