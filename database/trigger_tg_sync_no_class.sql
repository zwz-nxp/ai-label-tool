-- Trigger: tg_sync_no_class

-- DROP TRIGGER IF EXISTS tg_sync_no_class ON public.la_images;

CREATE OR REPLACE TRIGGER tg_sync_no_class
    BEFORE UPDATE OF is_no_class
    ON public.la_images
    FOR EACH ROW
    EXECUTE FUNCTION public.tgf_sync_no_class();