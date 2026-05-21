INSERT INTO fatigue_policy (id, name, department, min_rest_hours, max_consecutive_nights, max_weekly_hours, churn_threshold)
VALUES
    (1, 'Policy A — Standard', 'ICU', 11, 2, 60, 60),
    (2, 'Policy B — Strict Rest', 'ICU', 12, 2, 60, 60);

INSERT INTO staff (id, name, role, department, sensitivity_factor)
VALUES
    (1, 'Nguyen Thi Lan', 'NURSE', 'ICU', 1.0),
    (2, 'Tran Van Hung', 'NURSE', 'ICU', 1.0),
    (3, 'Le Thi Mai', 'NURSE', 'ICU', 1.0),
    (4, 'Pham Van Duc', 'DOCTOR', 'ICU', 1.0),
    (5, 'Hoang Thi Yen', 'NURSE', 'ICU', 1.0);

INSERT INTO shift (id, staff_id, start_at, end_at, shift_type, revision_count)
VALUES
    (1, 1, '2025-05-12 22:00:00+07', '2025-05-13 06:00:00+07', 'NIGHT', 2),
    (2, 1, '2025-05-13 14:00:00+07', '2025-05-13 22:00:00+07', 'DAY', 1),
    (3, 2, '2025-05-12 22:00:00+07', '2025-05-13 06:00:00+07', 'NIGHT', 0),
    (4, 2, '2025-05-13 22:00:00+07', '2025-05-14 06:00:00+07', 'NIGHT', 0),
    (5, 2, '2025-05-14 22:00:00+07', '2025-05-15 06:00:00+07', 'NIGHT', 0),
    (6, 3, '2025-05-12 07:00:00+07', '2025-05-12 16:00:00+07', 'DAY', 0),
    (7, 3, '2025-05-13 07:00:00+07', '2025-05-13 16:00:00+07', 'DAY', 0),
    (8, 3, '2025-05-14 07:00:00+07', '2025-05-14 16:00:00+07', 'DAY', 0),
    (9, 3, '2025-05-15 07:00:00+07', '2025-05-15 16:00:00+07', 'DAY', 0),
    (10, 3, '2025-05-16 07:00:00+07', '2025-05-16 16:00:00+07', 'DAY', 0),
    (11, 3, '2025-05-17 07:00:00+07', '2025-05-17 16:00:00+07', 'DAY', 0),
    (12, 3, '2025-05-18 07:00:00+07', '2025-05-18 16:00:00+07', 'DAY', 0),
    (13, 4, '2025-05-12 08:00:00+07', '2025-05-12 17:00:00+07', 'DAY', 0),
    (14, 4, '2025-05-14 08:00:00+07', '2025-05-14 17:00:00+07', 'DAY', 0),
    (15, 5, '2025-05-12 08:00:00+07', '2025-05-12 16:00:00+07', 'DAY', 6),
    (16, 5, '2025-05-14 08:00:00+07', '2025-05-14 16:00:00+07', 'DAY', 6);

INSERT INTO shift_revision (shift_id, changed_at, change_type)
VALUES
    (15, '2025-05-10 09:00:00+07', 'STAFF_REASSIGN'),
    (15, '2025-05-10 14:00:00+07', 'TIME_CHANGE'),
    (15, '2025-05-11 08:00:00+07', 'STAFF_REASSIGN'),
    (15, '2025-05-11 16:00:00+07', 'TIME_CHANGE'),
    (15, '2025-05-11 20:00:00+07', 'STAFF_REASSIGN'),
    (15, '2025-05-12 07:00:00+07', 'FINALIZED'),
    (16, '2025-05-10 10:00:00+07', 'STAFF_REASSIGN'),
    (16, '2025-05-11 09:00:00+07', 'TIME_CHANGE'),
    (16, '2025-05-11 15:00:00+07', 'STAFF_REASSIGN'),
    (16, '2025-05-12 08:00:00+07', 'TIME_CHANGE'),
    (16, '2025-05-13 07:00:00+07', 'STAFF_REASSIGN'),
    (16, '2025-05-13 18:00:00+07', 'FINALIZED');

SELECT setval('fatigue_policy_id_seq', (SELECT MAX(id) FROM fatigue_policy));
SELECT setval('staff_id_seq', (SELECT MAX(id) FROM staff));
SELECT setval('shift_id_seq', (SELECT MAX(id) FROM shift));
