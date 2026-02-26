-- Trigger: tg_do_snapshot

-- DROP TRIGGER IF EXISTS tg_do_snapshot ON public.la_snapshot;

CREATE OR REPLACE TRIGGER tg_do_snapshot
    AFTER INSERT
    ON public.la_snapshot
    FOR EACH ROW
    EXECUTE FUNCTION public.tgf_do_snapshot();

COMMENT ON TRIGGER tg_do_snapshot ON public.la_snapshot
    IS 'do snapshot by project id';