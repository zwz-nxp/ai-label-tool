-- Trigger: tg_update_image_isLabeled

-- DROP TRIGGER IF EXISTS "tg_update_image_isLabeled" ON public.la_images_label;

CREATE OR REPLACE TRIGGER "tg_update_image_isLabeled"
    AFTER INSERT
    ON public.la_images_label
    FOR EACH ROW
    EXECUTE FUNCTION public."tgf_update_isLabeled"();

COMMENT ON TRIGGER "tg_update_image_isLabeled" ON public.la_images_label
    IS 'update image isLabeled flag';