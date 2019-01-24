/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.rollback;

import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.content.Context;
import android.content.IntentSender;
import android.os.RemoteException;

import java.util.List;

/**
 * Offers the ability to rollback packages after upgrade.
 * <p>
 * For packages installed with rollbacks enabled, the RollbackManager can be
 * used to initiate rollback of those packages for a limited time period after
 * upgrade.
 *
 * @see PackageInstaller.SessionParams#setEnableRollback()
 * @hide
 */
@SystemApi
@SystemService(Context.ROLLBACK_SERVICE)
public final class RollbackManager {
    private final String mCallerPackageName;
    private final IRollbackManager mBinder;

    /** {@hide} */
    public RollbackManager(Context context, IRollbackManager binder) {
        mCallerPackageName = context.getPackageName();
        mBinder = binder;
    }

    /**
     * Returns a list of all currently available rollbacks.
     *
     * @throws SecurityException if the caller does not have the
     *            MANAGE_ROLLBACKS permission.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_ROLLBACKS)
    public List<RollbackInfo> getAvailableRollbacks() {
        try {
            return mBinder.getAvailableRollbacks().getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Gets the list of all recently committed rollbacks.
     * This is for the purposes of preventing re-install of a bad version of a
     * package and monitoring the status of a staged rollback.
     * <p>
     * Returns an empty list if there are no recently committed rollbacks.
     * <p>
     * To avoid having to keep around complete rollback history forever on a
     * device, the returned list of rollbacks is only guaranteed to include
     * rollbacks that are still relevant. A rollback is no longer considered
     * relevant if the package is subsequently uninstalled or upgraded
     * (without the possibility of rollback) to a higher version code than was
     * rolled back from.
     *
     * @return the recently committed rollbacks
     * @throws SecurityException if the caller does not have the
     *            MANAGE_ROLLBACKS permission.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_ROLLBACKS)
    public @NonNull List<RollbackInfo> getRecentlyCommittedRollbacks() {
        try {
            return mBinder.getRecentlyExecutedRollbacks().getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Commit the rollback with given id, rolling back all versions of the
     * packages to the last good versions previously installed on the device
     * as specified in the corresponding RollbackInfo object. The
     * rollback will fail if any of the installed packages or available
     * rollbacks are inconsistent with the versions specified in the given
     * rollback object, which can happen if a package has been updated or a
     * rollback expired since the rollback object was retrieved from
     * {@link #getAvailableRollbacks()}.
     * <p>
     * TODO: Specify the returns status codes.
     *
     * @param rollback to commit
     * @param statusReceiver where to deliver the results
     * @throws SecurityException if the caller does not have the
     *            MANAGE_ROLLBACKS permission.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_ROLLBACKS)
    public void commitRollback(@NonNull RollbackInfo rollback,
            @NonNull IntentSender statusReceiver) {
        try {
            mBinder.executeRollback(rollback, mCallerPackageName, statusReceiver);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Reload all persisted rollback data from device storage.
     * This API is meant to test that rollback state is properly preserved
     * across device reboot, by simulating what happens on reboot without
     * actually rebooting the device.
     *
     * @throws SecurityException if the caller does not have the
     *            MANAGE_ROLLBACKS permission.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_ROLLBACKS)
    public void reloadPersistedData() {
        try {
            mBinder.reloadPersistedData();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Expire the rollback data for a given package.
     * This API is meant to facilitate testing of rollback logic for
     * expiring rollback data.
     *
     * @param packageName the name of the package to expire data for.
     * @throws SecurityException if the caller does not have the
     *            MANAGE_ROLLBACKS permission.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_ROLLBACKS)
    public void expireRollbackForPackage(@NonNull String packageName) {
        try {
            mBinder.expireRollbackForPackage(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
