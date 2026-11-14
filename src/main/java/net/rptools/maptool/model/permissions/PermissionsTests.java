package net.rptools.maptool.model.permissions;

import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 A set of tests for determining access by an object for a Permissions permission. */
public interface PermissionsTests {
    /** Does an accessor object(e.g. Player) have permission generally */
    BiFunction<Permissions, Object, Boolean> getAccessorTest();

    /** Does an accessor object(e.g. Player) have permission generally */
    void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest);

    /**
     Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
     */
    TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest();

    /**
     Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
     */
    void setObjectAccessTest(
            @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest);

    /** Is the first object an ally of the second object. Used in other tests. */
    BiFunction<Object, Object, Boolean> getObjectAllyTest();

    /** Is the first object an ally of the second object. Used in other tests. */
    void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest);
}
