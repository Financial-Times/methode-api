package com.ft.methodeapi.repository;

import com.ft.methodeapi.service.Content;
import com.google.common.base.Optional;

public interface ContentRepository {

    Optional<Content> findContentByUuid(String uuid);

    public void close();
}
