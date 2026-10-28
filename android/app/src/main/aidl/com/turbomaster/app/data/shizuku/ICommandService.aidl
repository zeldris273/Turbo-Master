package com.turbomaster.app.data.shizuku;

import android.os.Bundle;

interface ICommandService {
    Bundle execute(in String[] commandTokens, long timeoutMs);
}
