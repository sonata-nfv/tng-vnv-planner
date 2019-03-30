insert into
  TEST_SUITE
  (ID, UUID)
  values
  (1, 'test_suite_uuid_1');

-- UPDATE SCENARIO
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A1', '1', '1', 'td_uuid_1', 'nsd_uuid_1', 'package_id_1', 'UPDATED');
-- CREATION SCENARIO
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A1', '1', '1', 'td_uuid_1', 'nsd_uuid_A', 'package_id_1', 'CREATED');
-- SCHEDULE SCENARIO
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A2', '1', '1', 'td_uuid_2', 'nsd_uuid_A', 'package_id_1', 'SCHEDULED');
-- REJECT SCENARIO
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A3', '1', '1', 'td_uuid_3', 'nsd_uuid_A', 'package_id_1', 'REJECTED');
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A4', '1', '1', 'td_uuid_4', 'nsd_uuid_A', 'package_id_1', 'REJECTED');
-- CONFIRMATION SCENARIO
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A5', '1', '1', 'td_uuid_5', 'nsd_uuid_A', 'package_id_1', 'CONFIRMED');
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A6', '1', '1', 'td_uuid_6', 'nsd_uuid_A', 'package_id_1', 'CONFIRMED');
-- REST SCENARIO'S: STARTING, COMPLETED, CANCELLING, CANCELLED, ERROR
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id, status)
  values
  (null, 'test_plan_uuid_old_A7', '1', '1', 'td_uuid_7', 'nsd_uuid_A', 'package_id_1', 'STARTING');

-- NEW TEST PLAN'S SCENARIO
insert into
  TEST_SUITE
  (ID, UUID)
  values
  (2, 'test_suite_uuid_2');

insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id)
  values
  (null, 'test_plan_uuid_new_B1', 1, '1', 'td_uuid_1', 'nsd_uuid_B', 'package_id_2');
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id)
  values
  (null, 'test_plan_uuid_new_B2', 2, '1', 'td_uuid_2', 'nsd_uuid_B', 'package_id_2');
insert into
  test_plan
  (id, uuid, index, test_suite_id, td_uuid, nsd_uuid, package_id)
  values
  (null, 'test_plan_uuid_new_B3', 1, '1', 'td_uuid_3', 'nsd_uuid_B', 'package_id_2');



