package net.rptools.maptool.model.permissions;

import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public abstract class GateKeeper implements PermissionsTests {
    protected BiFunction<Permissions, Object, Boolean> accessorTest;
    protected TriFunction<Permissions, Object, Object, Boolean> objectAccessTest;
    protected BiFunction<Object, Object, Boolean> objectAllyTest;

    public GateKeeper() {
    }

    @Override
    public BiFunction<Permissions, Object, Boolean> getAccessorTest() {
        return accessorTest;
    }

    @Override
    public void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest) {
        this.accessorTest = accessorTest;
    }

    @Override
    public TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest() {
        return objectAccessTest;
    }

    @Override
    public void setObjectAccessTest(
            @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest) {
        this.objectAccessTest = objectAccessTest;
    }

    @Override
    public BiFunction<Object, Object, Boolean> getObjectAllyTest() {
        return objectAllyTest;
    }

    @Override
    public void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest) {
        this.objectAllyTest = objectAllyTest;
    }
}
