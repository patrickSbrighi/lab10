package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String FILE_NAME = "config.yml";

    private int min;
    private int max;
    private int attemps;
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        try{
            readFile();
        } catch(Exception ex){
            for (final DrawNumberView view: views) {
                view.displayError(ex.getMessage());
            }
        }
        this.model = new DrawNumberImpl(this.min, this.max, this.attemps);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    private void readFile() throws IOException{
        try(final BufferedReader bf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(FILE_NAME)))){
            String line = null;
            while(( line = bf.readLine()) != null){
                String[] splitted = line.split(":");
                switch(splitted[0]){
                    case "minimum":
                        this.min = Integer.parseInt(splitted[1].trim());
                        break;
                    case "maximum":
                        this.max = Integer.parseInt(splitted[1].trim());
                        break;
                    case "attempts":
                        this.attemps = Integer.parseInt(splitted[1].trim());
                        break;
                }
            }
        } catch(IOException ex){
            throw ex;
        }
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), 
            new DrawNumberViewImpl(), 
            new PrintStreamView("new.log"), 
            new PrintStreamView(System.out));
    }

}
