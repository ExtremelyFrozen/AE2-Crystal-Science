package io.github.lounode.ae2cs.mixin.crystalindustry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Pseudo
@Mixin(targets = "com.minecart.yunxian.budding.BuddingFamilies", remap = false)
public abstract class CreateCrystalIndustryFluixMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, require = 0)
    private static void ae2cs$skipFluixFamily(@Coerce Object spec, CallbackInfoReturnable<Object> cir) {
        try {
            if (!"fluix".equals(spec.getClass().getMethod("id").invoke(spec))) {
                return;
            }

            Class<?> familyClass = Class.forName(
                    "com.minecart.yunxian.budding.BuddingFamilies$RegisteredFamily",
                    false,
                    spec.getClass().getClassLoader());
            Constructor<?> familyConstructor = findFamilyConstructor(familyClass, spec.getClass());
            cir.setReturnValue(familyConstructor.newInstance(spec, null, null, null, null, null));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to suppress Create: Crystal Industry's Fluix family", exception);
        }
    }

    private static Constructor<?> findFamilyConstructor(Class<?> familyClass, Class<?> specClass)
                                                                                                  throws NoSuchMethodException {
        for (Constructor<?> constructor : familyClass.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 6 && parameterTypes[0].isAssignableFrom(specClass)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("RegisteredFamily(spec, null, null, null, null, null)");
    }
}
