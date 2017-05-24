package com.aed.swm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import java.util.Arrays;

public class SWM implements ApplicationListener {
    private Color[] colors = new Color[]{Color.WHITE, Color.YELLOW, Color.BLUE, Color.GREEN, Color.PINK};
    private CustomShapeRenderer renderer;
    private Array<Box> levelBoxes = new Array<Box>();
    private Array<LittleBox> littleBoxes = new Array<LittleBox>();
    private ProgressBar bar;
    private Stage stage;
    private Stage ui;

    private Array<Boolean> attempts = new Array<Boolean>();

    private Vector2 nextLittleBoxPos = new Vector2(10, 10);

    private int numSets = 3, numBoxes = 5, currentSet = 1;

    public Vector2 nextLittleBoxPos() {
        Vector2 clone = nextLittleBoxPos.cpy();
        nextLittleBoxPos = nextLittleBoxPos.add(LittleBox.SHAPE_WIDTH + 10, 0);
        return clone;
    }

    public CustomShapeRenderer getRenderer() {
        return this.renderer;
    }

    @Override
    public void create() {
        stage = new Stage(new ScalingViewport(Scaling.fillX, 480, 800));
        ui = new Stage(new ScalingViewport(Scaling.fillX, 480, 800));
        renderer = new CustomShapeRenderer();
        bar = new ProgressBar(renderer, 10);
        Gdx.input.setInputProcessor(stage);
        ui.addActor(bar);

        resetGame();
    }

    public Array<Box> generateBoxes(int count, Color[] colors) {
        Array<Box> generatedBoxes = new Array<Box>();

        int candidateX = 0, candidateY = 0;
        for (int i = 0; i < count; i++) {
            boolean unique = false;
            while (!unique) {
                candidateX = MathUtils.random(0, 480 - Box.SHAPE_WIDTH);
                candidateY = MathUtils.random(100, 800 - Box.SHAPE_HEIGHT); // 100 margin from bottom.

                Rectangle candidateRectangle = new Rectangle(candidateX, candidateY, Box.SHAPE_WIDTH + 20f, Box.SHAPE_HEIGHT + 20f);

                unique = true;
                for (Box box : generatedBoxes) {
                    if (candidateRectangle.overlaps(new Rectangle(box.getX(), box.getY(), box.getWidth(), box.getHeight()))) {
                        unique = false;
                        break;
                    }
                }
            }
            Box box = new Box(this, candidateX, candidateY);

            generatedBoxes.add(box);
        }
        return generatedBoxes;
    }

    @Override
    public void resize(int width, int height) {

    }

    public void recordAttempt(boolean littleBoxUnhidden) {
        attempts.add(littleBoxUnhidden);

        if (littleBoxUnhidden) {
            boolean allUncovered = true;
            for (LittleBox box : littleBoxes) {
                if (!box.isUncovered()) {
                    allUncovered = false;
                    break;
                }
            }

            if (littleBoxes.size >= numBoxes && allUncovered) {
                // Game finished; next set.
                if (currentSet >= numSets) {
                    // Game done.
                    System.out.println("GAME DONE");
                } else {
                    currentSet++;
                    resetGame();
                }
            }
        }

        System.out.println(Arrays.toString(attempts.toArray()));
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setProjectionMatrix(stage.getBatch().getProjectionMatrix());
        renderer.setTransformMatrix(stage.getBatch().getTransformMatrix());

//		bar.addAction(Actions.moveTo(currentLevel / (float) (numBoxes * numSets), bar.getY(), 0.2f, Interpolation.pow2InInverse));

        float delta = Gdx.graphics.getDeltaTime();
        stage.act(delta);
        ui.act(delta);
        stage.draw();
        ui.draw();
    }

    public void resetGame() {
        nextLittleBoxPos = new Vector2(10, 10);

        for (Box box : levelBoxes) box.remove();
        levelBoxes.clear();

        for (LittleBox box : littleBoxes) box.remove();
        littleBoxes.clear();

        levelBoxes.addAll(generateBoxes(numBoxes, null));
        for (Box box : levelBoxes) {
            stage.addActor(box);
//			box.addAction(
//					parallel(
//							sequence(alpha(0), delay(0.5f), fadeIn(0.5f)),
//							repeat(5, sequence(
//									scaleTo(1.1f, 1.1f, presentationDuration / 10),
//									scaleTo(1f, 1f, presentationDuration / 10)
//							))
//					));
        }

        assignLittleBox();
    }

    public void assignLittleBox() {
        if (littleBoxes.size >= numBoxes) return;

        Box randomBox = levelBoxes.get(littleBoxes.size);

        LittleBox littleBox = new LittleBox(this, Color.YELLOW, randomBox.getX() + Box.SHAPE_WIDTH / 2, randomBox.getY() + Box.SHAPE_HEIGHT / 2);
        littleBoxes.add(littleBox);

        randomBox.setLittleBox(littleBox);

        stage.addActor(littleBox);

        littleBox.toBack();
        for (Box box : levelBoxes) box.toFront();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        renderer.dispose();
    }
}
