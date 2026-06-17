UPDATE project_attribute
SET value = extract(epoch FROM value::interval)::BIGINT
WHERE attribute_id IN (SELECT id
                       FROM attribute
                       WHERE attribute.name IN ('job.interruptJobTime',
                                                'job.keepLaunches',
                                                'job.keepLogs',
                                                'job.keepScreenshots'))
  and value != 'forever';

UPDATE project_attribute
SET value = '0'
WHERE attribute_id IN (SELECT id
                       FROM attribute
                       WHERE attribute.name IN ('job.interruptJobTime',
                                                'job.keepLaunches',
                                                'job.keepLogs',
                                                'job.keepScreenshots'))
  AND value = 'forever';