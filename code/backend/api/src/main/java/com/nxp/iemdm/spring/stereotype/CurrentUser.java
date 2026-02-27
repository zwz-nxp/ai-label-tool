package com.nxp.iemdm.spring.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Deprecated(
    since =
        "24th of June 2024. Replaced by @AuthenticationPrincipal IEMDMPrincipal, which includes roles in contrast with currentUser which does not already include roles",
    forRemoval = true)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {}
