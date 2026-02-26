-- Trigger: tg_generate_track_id

-- DROP TRIGGER IF EXISTS tg_generate_track_id ON public.la_training_record;

CREATE OR REPLACE TRIGGER tg_generate_track_id
    BEFORE INSERT
    ON public.la_training_record
    FOR EACH ROW
    EXECUTE FUNCTION public.tgf_generate_track_id();