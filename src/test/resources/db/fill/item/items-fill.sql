SELECT items_init();

INSERT INTO clusters(id, index_id, project_id, launch_id, message)
VALUES (1, 1, 1, 1, 'Message');

INSERT INTO clusters_test_item(cluster_id, item_id)
VALUES (1, 3);
INSERT INTO clusters_test_item(cluster_id, item_id)
VALUES (1, 4);
INSERT INTO clusters_test_item(cluster_id, item_id)
VALUES (1, 5);
INSERT INTO clusters_test_item(cluster_id, item_id)
VALUES (1, 6);

UPDATE log
SET cluster_id = 1
WHERE id IN (4, 5, 6);