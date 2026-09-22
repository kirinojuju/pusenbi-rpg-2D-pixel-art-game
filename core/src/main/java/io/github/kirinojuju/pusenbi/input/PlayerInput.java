package io.github.kirinojuju.pusenbi.input;

/** Device-independent control state. Reading a value does not consume it. */
public interface PlayerInput {
    /** Horizontal direction in [-1, 1]; positive means right. */
    float getMoveX();

    /** Vertical direction in [-1, 1]; positive means up. */
    float getMoveY();

    /** Whether attack is held. Player owns the press-edge detection. */
    boolean isAttackPressed();

    /** Whether dash is held. Player owns the press-edge detection. */
    boolean isDashPressed();
}
