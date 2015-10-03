package hu.ait.android.minesweeper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import hu.ait.android.minesweeper.MainActivity;
import hu.ait.android.minesweeper.R;
import hu.ait.android.minesweeper.model.MSModel;

/*
    Viewing class for MineSweeper Game.
    Visualizes the minesweeper as following :
        - Represents the whole grid as grid of rectangles
        - Represents mines with images
        - Represents minecount of a cell with different colored numbers
        - Shows a X when the game is lost
 */
public class MSView extends View {

    private Paint paintLine, paintCell, paintNumCell,           // All the paints used for drawing
            paintCross, paintNumber;
    private int gameWidth = 5, gameHeight = 5;                  // Dimension of the game
    private int xPos = -1, yPos = -1;                           // Keeps track of clicked location
    private int widthMultiplier, heightMultiplier;              // Used to resize cells and letters

    private boolean firstMove = true;                           // booleans used for program
    private boolean gamePlaying = true;
    private boolean gameLost = false;
    private boolean longPressed;
    private boolean firstClick = true;

    private Bitmap mineImage;                                   // Image resources
    private Bitmap flagImage;

    public MSView(Context context, AttributeSet attrs) {
        super(context, attrs);

        definePaints();

        MSModel.getInstance().setupGame(gameWidth, gameHeight);

        mineImage = BitmapFactory.decodeResource(getResources(), R.drawable.mine);
        flagImage = BitmapFactory.decodeResource(getResources(), R.drawable.flag);
    }

    // Declare all the paints to be used for the program
    private void definePaints() {
        paintLine = new Paint();
        paintLine.setColor(Color.WHITE);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);

        paintCell = new Paint();
        paintCell.setColor(Color.GRAY);
        paintCell.setStyle(Paint.Style.FILL);

        paintNumCell = new Paint();
        paintNumCell.setColor(Color.LTGRAY);
        paintNumCell.setStyle(Paint.Style.FILL);

        paintCross = new Paint();
        paintCross.setColor(Color.RED);
        paintCross.setStyle(Paint.Style.STROKE);
        paintCross.setStrokeWidth(20);

        paintNumber = new Paint();
        paintNumber.setColor(Color.BLACK);
        paintNumber.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawGameArea(canvas);
        drawStatus(canvas);

        //Draw a cross if game was lost
        if (gameLost) {
            canvas.drawLine(0, 0, getWidth(), getHeight(), paintCross);
            canvas.drawLine(0, getHeight(), getWidth(), 0, paintCross);
        }

        if (MSModel.getInstance().gameWon()) {
            winGame();
        }
    }

    // Draw the game area with lines
    private void drawGameArea(Canvas canvas) {
        widthMultiplier = getWidth() / gameWidth;
        heightMultiplier = getHeight() / gameHeight;
        canvas.drawRect(0, 0, gameWidth * widthMultiplier, gameHeight * heightMultiplier, paintCell);


        for (int i = 0; i <= gameWidth; i++) {
            canvas.drawLine(i * widthMultiplier, 0, i * widthMultiplier, getHeight(), paintLine);
        }
        for (int j = 0; j <= gameHeight; j++) {
            canvas.drawLine(0, j * heightMultiplier, getWidth(), j * heightMultiplier, paintLine);
        }
    }


    //Draw the current gameboard
    private void drawStatus(Canvas canvas) {

        int minMultiplier = (widthMultiplier < heightMultiplier) ?
                widthMultiplier : heightMultiplier;
        paintNumber.setTextSize(minMultiplier);

        for (int i = 0; i < gameWidth; i++) {
            for (int j = 0; j < gameHeight; j++) {
                processContentAt(canvas, i, j);
            }
        }
    }

    // Process each cell and show the content accordinlgy
    private void processContentAt(Canvas canvas, int i, int j) {
        short content = MSModel.getInstance().getCellContent(i, j);

        switch (content) {
            case MSModel.EMPTY:
                break;
            case MSModel.FLAG:
                canvas.drawBitmap(getResizedBitmap(flagImage, widthMultiplier, heightMultiplier),
                        i * widthMultiplier, j * heightMultiplier, null);
                break;
            default:
                drawNumberAt(canvas, i, j, content);
                break;
        }

        if (gameLost) {
            drawBombAt(canvas, i, j);
        }
    }

    // Draw the number for the location
    private void drawNumberAt(Canvas canvas, int i, int j, short content) {
        canvas.drawRect(i * widthMultiplier + 2, j * heightMultiplier + 2,
                (i + 1) * widthMultiplier - 2, (j + 1) * heightMultiplier - 2, paintNumCell);

        switchPaintColorFor(content);

        canvas.drawText(Integer.toString(content),
                (float) ((i + 0.25) * widthMultiplier),
                (float) ((j + 0.75) * heightMultiplier),
                paintNumber);
    }

    //Change the color of paint according to the count
    private void switchPaintColorFor(short content) {
        switch (content) {
            case 0:
                paintNumber.setColor(paintNumCell.getColor());
                break;
            case 1:
                paintNumber.setColor(Color.RED);
                break;
            case 2:
                paintNumber.setColor(Color.BLUE);
                break;
            default:
                paintNumber.setColor(Color.GREEN);
                break;
        }
    }

    private void drawBombAt(Canvas canvas, int i, int j) {
        if (MSModel.getInstance().bombIsAt(i, j)) {
            canvas.drawBitmap(getResizedBitmap(mineImage, widthMultiplier, heightMultiplier),
                    i * widthMultiplier, j * heightMultiplier, null);
        }
    }


    // Handler for long pressed (for flags)
    // Performs the following action if user pressed on for a long time
    final Handler handler = new Handler();
    Runnable rLongPressed = new Runnable() {
        public void run() {
            longPressed = true;
            if (!firstClick && !MSModel.getInstance().clickFlag(xPos, yPos)) {
                loseGame(getContext().getString(R.string.statement_noBomb));
            } else if(!firstClick) {
                ((MainActivity) getContext()).setNumMine(
                        getContext().getString(R.string.remaining_mines) + MSModel.getInstance().getRemainingBombCount());
            }
            invalidate();
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gamePlaying) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                processClickDown(event);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                processClickUp();
                return false;
            }
        }
        return super.onTouchEvent(event);
    }


    private void processClickDown(MotionEvent event) {
        xPos = ((int) event.getX()) / (getWidth() / gameWidth);
        yPos = ((int) event.getY()) / (getHeight() / gameHeight);

        sanityCheck();

        //If it was clicked for first time, start timer
        if (firstMove) {
            ((MainActivity) getContext()).startTimer();
            firstMove = false;
        }

        // start counting time for long pressed
        handler.postDelayed(rLongPressed, 500);
        longPressed = false;
    }

    //In case click goes out of bound, do sanity check
    // (Ideally, shout not be called)
    private void sanityCheck() {
        if(xPos == gameWidth){
            xPos -= 1;
        }
        if(yPos == gameHeight){
            yPos -= 1;
        }
    }

    private void processClickUp() {
        // Remove the call to the long pressed if there is one since it is not longpressed
        handler.removeCallbacks(rLongPressed);
        if (!longPressed) {
            //If it was fist click, set up the bombs
            if (firstClick) {
                MSModel.getInstance().setupBombs(xPos, yPos);
                firstClick = false;
            }

            //If you clicked on the bomb
            if (!MSModel.getInstance().click(xPos, yPos)) {
                loseGame(getContext().getString(R.string.msg_bombClicked));
            }
            ((MainActivity) getContext()).setNumMine(
                    getContext().getString(R.string.remaining_mines) + MSModel.getInstance().getRemainingBombCount());
            invalidate();
        }
    }

    private void winGame() {
        Toast.makeText(((MainActivity) getContext()), R.string.msg_won, Toast.LENGTH_LONG).show();
        gamePlaying = false;
        ((MainActivity) getContext()).stopTimer();
    }

    private void loseGame(String s) {
        Toast.makeText(((MainActivity) getContext()), s, Toast.LENGTH_LONG).show();
        gamePlaying = false;
        gameLost = true;
        ((MainActivity) getContext()).stopTimer();
    }


    public void resetGame() {
        MSModel.getInstance().resetModel();
        firstClick = true;
        gamePlaying = true;
        gameLost = false;
        firstMove = true;
        ((MainActivity) getContext()).resetTimer();
        ((MainActivity) getContext()).setNumMine(
                getContext().getString(R.string.remaining_mines) + MSModel.getInstance().getRemainingBombCount());
        invalidate();
    }

    //Choose shorter length out of height and width
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
        setMeasuredDimension(d, d);
    }

    // Resize the image to needed size
    public Bitmap getResizedBitmap(Bitmap original, int width, int height) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(original, width, height, false);
        return resizedBitmap;
    }

    // Set the dimension of the game
    public void setDimension(int width, int height) {
        gameWidth = width;
        gameHeight = height;
        MSModel.getInstance().resetModel(width, height);
        invalidate();
    }

}
