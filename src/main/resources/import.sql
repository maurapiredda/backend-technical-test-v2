insert into address (id, street, postcode, city, country) values (1, '785 Cabell Avenue', '23219', 'Raccoon', 'USA');

insert into customer (id, first_name, last_name, telephone, email) values (1, 'Leon', 'Kennedy', '+1 7035555015', 'leon.kennedy@rpd.com');
insert into customer (id, first_name, last_name, telephone, email) values (2, 'Clair', 'Redfield', '+1 7577149738', 'clair.redfield@terrasave.com');

insert into pilotes_order(id, order_number, customer_id, address_id, pilotes_number, total, creation_date, notified) values (1, '0000000001', 1, 1, 'FIVE', 5.0,  {ts '2021-09-20T15:00:00Z'}, false);
insert into pilotes_order(id, order_number, customer_id, address_id, pilotes_number, total, creation_date, notified) values (2, '0000000002', 1, 1, 'TEN', 10.0,  {ts '2021-09-21T15:10:00Z'}, false);
insert into pilotes_order(id, order_number, customer_id, address_id, pilotes_number, total, creation_date, notified) values (3, '0000000003', 2, 1, 'FIVE', 5.0,  {ts '2021-09-22T15:20:00Z'}, false);
