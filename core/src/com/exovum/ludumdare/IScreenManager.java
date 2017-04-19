package com.exovum.ludumdare;

import com.badlogic.gdx.Screen;

public interface IScreenManager {

    void endCurrentScreen();
    Screen getNextScreen();
}
