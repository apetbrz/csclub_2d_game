package main;

import javax.swing.*;

public class Game implements Runnable {

    //window: the actual Windows window that the game will be contained in
    public JFrame window;

    //panel: the "screen" that the game draws to
    public GamePanel panel;

    //state: where all the data for the game is stored, the current "state" of it
    //state also contains game logic
    public GameState state;

    //thread: a "thread" of execution, this will actually *run* our game in the cpu
    private Thread thread;

    //controller: this handles all user input (mouse/keyboard)
    public ControlHandler controller;

    //running: while the game is running, this is true
    //(to prevent the game for running multiple times at the same time)
    private boolean running = false;

    //CONSTRUCTOR
    public Game() {
        //create the game state
        state = new GameState(this);

        //initialize window settings
        window = new JFrame();              //create the object
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //close window = exit program
        window.setResizable(false);         //prevent user resizing (by dragging)
        window.setTitle(Main.TITLE);        //set title (from Main)
        window.setIconImage(FileHandler.loadImage(Main.ICON_FILENAME)); //set icon (from Main)

        //initialize the "panel" - the screen that will be drawn on
        panel = new GamePanel(state);

        //add the panel to the window
        window.add(panel);

        //"pack" - set the window size to the max size of its elements
        //in this case, set the window size to the panel size
        window.pack();

        //create controller object
        controller = new ControlHandler();

        //link it to the game logic
        state.linkController(controller);

        //add "listeners" to the panel,
        //these let us "hear" when the user clicks/presses a button/moves mouse
        panel.addKeyListener(controller);
        panel.addMouseListener(controller);
        panel.addMouseMotionListener(controller);

        //finally, set the window to be visible
        window.setVisible(true);
    }// END Game CONSTRUCTOR

    //start() - initializes the thread that runs the game, and starts it
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    //run() is called when the thread starts (thread.start();)
    //this is where the game loop happens
    //the game loop is the cycle of
    //  update the game
    //  draw it to the screen
    //where everything actually happens
    public void run() {
        if (running) return;    //cancel run() call if already running
        running = true;         //otherwise, we are now running

        //these variables are for measuring frame times (how we calculate FPS)
        //they are stored as NANOSECONDS
        long frameStartTimeNano;    //when the frame is started
        long gameUpdateTimeNano;    //when the game is done updating its logic
        long frameEndTimeNano;      //when the panel is done updating the screen
        long frameTimeNano;         //total time from start->end of game update sequence
        double totalFrameTimeNano;  //total time from start->end of frame (incl. sleep)

        //millisecondsPerFrame is our target render time, to reach the TARGET_FPS from Main
        double millisecondsPerFrame = 1000.0/Main.TARGET_FPS;

        //the thread should never be null, so this should loop forever (until we quit)
        //this is the MAIN GAME LOOP! this is what a "frame" is!!
        while (thread != null) {
            //save frame start time
            frameStartTimeNano = System.nanoTime();

            //update the game
            state.update();

            //save game update end time, CURRENTLY UNUSED
            gameUpdateTimeNano = System.nanoTime();

            //update the screen
            panel.update();

            //save screen update end time
            frameEndTimeNano = System.nanoTime();

            //calculate how long it took to render everything
            frameTimeNano = frameEndTimeNano-frameStartTimeNano;

            //in order to force the game to stay at a constant fps of TARGET_FPS (from Main),
            //we need to make sure frames only happen every millisecondsPerFrame milliseconds
            //so, after the render is done, sleep for the remaining time:
            try {
                //frameTimeNano is in nanoseconds, so we divide it by 1,000,000 to make it milliseconds
                //subtract frameTimeMilli from millisecondsPerFrame to get the remaining time,
                //which we tell the program to just sleep through, until the next frame
                double frameTimeMilli = frameTimeNano/1000000.0;
                long sleepTime = (long)(millisecondsPerFrame - frameTimeMilli);
                Thread.sleep(Math.max(0,sleepTime));
                //sleep() may be "interrupted" by other things, so we have to catch that
                //InterruptedException, and just log that it happened (but continue running anyways)
            } catch (InterruptedException e) {
                Logger.log(1,"THREAD INTERRUPTED DURING GAME LOOP SLEEP");
            }

            long finalTimeNano = System.nanoTime();
            //totalFrameTimeNano is the total time from start->end of the entire frame
            // (including sleep), which we use for FPS display
            totalFrameTimeNano = (finalTimeNano - frameStartTimeNano);

            //finally, tell the panel to update the FPS display with that total time.
            panel.updateFPS(totalFrameTimeNano / 1000000.0);
            panel.updateFramePercentage(frameTimeNano*100/totalFrameTimeNano);

            if(running == false) break;

        }//end game loop



    }//end run()

    public void gameOver() {
        running = false;
    }

    public boolean isOver() {
        return !running;
    }
}