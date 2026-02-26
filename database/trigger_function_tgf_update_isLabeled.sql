-- FUNCTION: public.tgf_update_isLabeled()

-- DROP FUNCTION IF EXISTS public."tgf_update_isLabeled"();

CREATE OR REPLACE FUNCTION public."tgf_update_isLabeled"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
declare
   cn_image_count integer;
begin
   select count(1) into cn_image_count from public.la_images
   where id=NEW.image_id
   ;
   if cn_image_count>0 then
	  update public.la_images
	  set is_labeled=true
	  where id=NEW.image_id
	  ;
   end if;
   return NEW;
end;
$BODY$;

ALTER FUNCTION public."tgf_update_isLabeled"()
    OWNER TO postgres;

COMMENT ON FUNCTION public."tgf_update_isLabeled"()
    IS 'update isLabeled flag after insert in la_images_label table.';
