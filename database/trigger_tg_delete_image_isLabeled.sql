-- Trigger: tg_delete_image_isLabeled

-- DROP TRIGGER IF EXISTS "tg_delete_image_isLabeled" ON public.la_images_label;

CREATE OR REPLACE TRIGGER "tg_delete_image_isLabeled"
    AFTER DELETE
    ON public.la_images_label
    FOR EACH ROW
    EXECUTE FUNCTION public."tgf_delete_isLabeled"();

COMMENT ON TRIGGER "tg_delete_image_isLabeled" ON public.la_images_label
    IS 'set image isLabeled as false';