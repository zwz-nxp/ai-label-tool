-- PROCEDURE: public.restore_snapshot_data(bigint, bigint)
-- 
-- Purpose: Restore all snapshot data from _ss tables to current tables
-- preserving original IDs to maintain referential integrity and file_id links.
-- 
-- Parameters:
--   p_snapshot_id: The snapshot ID to restore from
--   p_project_id: The target project ID to restore to
--
-- Returns: JSON object with restoration statistics
--
-- Usage: SELECT restore_snapshot_data(123, 456);

CREATE OR REPLACE FUNCTION public.restore_snapshot_data(
    p_snapshot_id bigint,
    p_project_id bigint
)
RETURNS json
LANGUAGE plpgsql
AS $$
DECLARE
    v_stats json;
    v_classes_restored integer := 0;
    v_tags_restored integer := 0;
    v_metadata_restored integer := 0;
    v_splits_restored integer := 0;
    v_images_restored integer := 0;
    v_labels_restored integer := 0;
    v_image_tags_restored integer := 0;
    v_image_metadata_restored integer := 0;
    v_max_id bigint;
BEGIN
    RAISE NOTICE 'Starting snapshot restoration: snapshot_id=%, project_id=%', p_snapshot_id, p_project_id;

    -- ========================================
    -- STEP 1: Restore Project Classes
    -- ========================================
    RAISE NOTICE 'Restoring project classes...';
    
    INSERT INTO public.la_project_class (id, project_id, class_name, description, color_code, created_at, created_by)
    SELECT 
        id,
        p_project_id,
        class_name,
        description,
        color_code,
        created_at,
        created_by
    FROM public.la_project_class_ss
    WHERE snapshot_id = p_snapshot_id;
    
    GET DIAGNOSTICS v_classes_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % project classes', v_classes_restored;
    
    -- Update sequence for la_project_class
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_project_class;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 2: Restore Project Tags
    -- ========================================
    RAISE NOTICE 'Restoring project tags...';
    
    INSERT INTO public.la_project_tag (id, project_id, name, created_at, created_by)
    SELECT 
        id,
        p_project_id,
        name,
        created_at,
        created_by
    FROM public.la_project_tag_ss
    WHERE snapshot_id = p_snapshot_id;
    
    GET DIAGNOSTICS v_tags_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % project tags', v_tags_restored;
    
    -- Update sequence for la_project_tag
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_project_tag;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 3: Restore Project Metadata
    -- ========================================
    RAISE NOTICE 'Restoring project metadata...';
    
    INSERT INTO public.la_project_metadata (id, project_id, name, type, value_from, predefined_values, multiple_values, created_at, created_by)
    SELECT 
        id,
        p_project_id,
        name,
        type,
        value_from,
        predefined_values,
        multiple_values,
        created_at,
        created_by
    FROM public.la_project_metadata_ss
    WHERE snapshot_id = p_snapshot_id;
    
    GET DIAGNOSTICS v_metadata_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % project metadata', v_metadata_restored;
    
    -- Update sequence for la_project_metadata
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_project_metadata;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 4: Restore Project Splits
    -- ========================================
    RAISE NOTICE 'Restoring project splits...';
    
    INSERT INTO public.la_project_split (id, project_id, train_ratio, dev_ratio, test_ratio, class_id, created_at, created_by)
    SELECT 
        id,
        p_project_id,
        train_ratio,
        dev_ratio,
        test_ratio,
        class_id,
        created_at,
        created_by
    FROM public.la_project_split_ss
    WHERE snapshot_id = p_snapshot_id;
    
    GET DIAGNOSTICS v_splits_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % project splits', v_splits_restored;
    
    -- Update sequence for la_project_split
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_project_split;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 5: Restore Images
    -- ========================================
    RAISE NOTICE 'Restoring images...';
    
    -- Note: is_labeled is NOT set here - it will be managed by database triggers
    -- after labels are restored
    INSERT INTO public.la_images (
        id, project_id, file_name, file_size, width, height, split, is_no_class,
        thumbnail_image, thumbnail_width_ratio, thumbnail_height_ratio, file_id,
        created_at, created_by
    )
    SELECT 
        id,
        p_project_id,
        file_name,
        file_size,
        width,
        height,
        split,
        is_no_class,
        thumbnail_image,
        thumbnail_width_ratio,
        thumbnail_height_ratio,
        file_id,  -- Preserve file_id to maintain link to shared image files
        created_at,
        created_by
    FROM public.la_images_ss
    WHERE snapshot_id = p_snapshot_id;
    
    GET DIAGNOSTICS v_images_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % images', v_images_restored;
    
    -- Update sequence for la_images
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_images;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 6: Restore Image Labels
    -- ========================================
    RAISE NOTICE 'Restoring image labels...';
    
    -- Only restore labels where both image and class exist
    INSERT INTO public.la_images_label (id, image_id, class_id, position, created_at, created_by)
    SELECT 
        sl.id,
        sl.image_id,
        sl.class_id,
        sl.position,
        sl.created_at,
        sl.created_by
    FROM public.la_images_label_ss sl
    WHERE sl.snapshot_id = p_snapshot_id
      AND EXISTS (SELECT 1 FROM public.la_images WHERE id = sl.image_id)
      AND EXISTS (SELECT 1 FROM public.la_project_class WHERE id = sl.class_id);
    
    GET DIAGNOSTICS v_labels_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % image labels', v_labels_restored;
    
    -- Update sequence for la_images_label
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_images_label;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 7: Restore Image Tags
    -- ========================================
    RAISE NOTICE 'Restoring image tags...';
    
    -- Only restore tags where both image and tag exist
    INSERT INTO public.la_images_tag (id, image_id, tag_id, created_at, created_by)
    SELECT 
        st.id,
        st.image_id,
        st.tag_id,
        st.created_at,
        st.created_by
    FROM public.la_images_tag_ss st
    WHERE st.snapshot_id = p_snapshot_id
      AND EXISTS (SELECT 1 FROM public.la_images WHERE id = st.image_id)
      AND EXISTS (SELECT 1 FROM public.la_project_tag WHERE id = st.tag_id);
    
    GET DIAGNOSTICS v_image_tags_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % image tags', v_image_tags_restored;
    
    -- Update sequence for la_images_tag
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_images_tag;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- STEP 8: Restore Image Metadata
    -- ========================================
    RAISE NOTICE 'Restoring image metadata...';
    
    -- Only restore metadata where both image and metadata definition exist
    INSERT INTO public.la_images_metadata (id, image_id, metadata_id, value, created_at, created_by)
    SELECT 
        sm.id,
        sm.image_id,
        sm.metadata_id,
        sm.value,
        sm.created_at,
        sm.created_by
    FROM public.la_images_metadata_ss sm
    WHERE sm.snapshot_id = p_snapshot_id
      AND EXISTS (SELECT 1 FROM public.la_images WHERE id = sm.image_id)
      AND EXISTS (SELECT 1 FROM public.la_project_metadata WHERE id = sm.metadata_id);
    
    GET DIAGNOSTICS v_image_metadata_restored = ROW_COUNT;
    RAISE NOTICE 'Restored % image metadata', v_image_metadata_restored;
    
    -- Update sequence for la_images_metadata
    SELECT COALESCE(MAX(id), 0) INTO v_max_id FROM public.la_images_metadata;
    PERFORM setval('hibernate_sequence', GREATEST(v_max_id, currval('hibernate_sequence')));

    -- ========================================
    -- Build statistics JSON
    -- ========================================
    v_stats := json_build_object(
        'success', true,
        'snapshot_id', p_snapshot_id,
        'project_id', p_project_id,
        'classes_restored', v_classes_restored,
        'tags_restored', v_tags_restored,
        'metadata_restored', v_metadata_restored,
        'splits_restored', v_splits_restored,
        'images_restored', v_images_restored,
        'labels_restored', v_labels_restored,
        'image_tags_restored', v_image_tags_restored,
        'image_metadata_restored', v_image_metadata_restored
    );

    RAISE NOTICE 'Snapshot restoration completed successfully';
    RAISE NOTICE 'Statistics: %', v_stats;

    RETURN v_stats;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Snapshot restoration failed: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;

-- Grant execute permission
ALTER FUNCTION public.restore_snapshot_data(bigint, bigint) OWNER TO postgres;

COMMENT ON FUNCTION public.restore_snapshot_data(bigint, bigint) IS 
'Restores all snapshot data from _ss tables to current tables preserving original IDs. 
This ensures referential integrity and maintains file_id links to shared image files.
The is_labeled flag is managed by database triggers after restoration completes.';
