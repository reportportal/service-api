package com.epam.ta.reportportal.core.tms.service;

public interface CrudService<IN,OUT,ID> {
    OUT create(final long projectId,IN t);
    OUT update(final long projectId,ID id, IN t);
    void delete(final long projectId,ID id);
    OUT getById(final long projectId,ID id);
}
