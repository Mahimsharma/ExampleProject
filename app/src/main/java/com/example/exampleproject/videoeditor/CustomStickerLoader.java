package com.example.exampleproject.videoeditor;

import com.banuba.sdk.veui.data.StickerLoader;
import com.banuba.sdk.veui.domain.StickerBundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

public class CustomStickerLoader implements StickerLoader {

    public CustomStickerLoader(){

    }
    @Nullable
    @Override
    public Object loadStickerFile(@NotNull File file, @NotNull String url, @NotNull Continuation<? super Unit> continuation) {


        return null;
    }

    @Nullable
    @Override
    public Object loadStickers(@NotNull String query, int offset, @NotNull Continuation<? super StickerBundle> continuation){



        return null;
    }
}