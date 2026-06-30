UPDATE filter_condition
SET value           = REPLACE(value, ',', ':,') || ':',
    search_criteria = 'compositeAttribute'
WHERE search_criteria = 'attributeKey';

UPDATE filter_condition
SET search_criteria = 'compositeAttribute'
WHERE search_criteria = 'attributeValue';