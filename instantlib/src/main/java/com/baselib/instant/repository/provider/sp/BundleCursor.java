package com.baselib.instant.repository.provider.sp;

import android.database.MatrixCursor;
import android.os.Bundle;

/**
 * @author wsb
 */
final class BundleCursor extends MatrixCursor {
    private Bundle mBundle;

    public BundleCursor(Bundle extras) {
        super(new String[]{}, 0);
        mBundle = extras;
    }

    @Override
    public Bundle getExtras() {
        return mBundle;
    }

    @Override
    public Bundle respond(Bundle extras) {
        mBundle = extras;
        return mBundle;
    }
}