/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.integration;


import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.image.ImageParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Andrei Varabyeu
 */
public class AbstractUserReplicator {

//    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserReplicator.class);
//
//    protected final UserRepository userRepository;
//    protected final ProjectRepository projectRepository;
//    protected final PersonalProjectService personalProjectService;
//    protected final DataStorage dataStorage;
//
//    public AbstractUserReplicator(UserRepository userRepository, ProjectRepository projectRepository,
//                                  PersonalProjectService personalProjectService, DataStorage dataStorage) {
//        this.userRepository = userRepository;
//        this.projectRepository = projectRepository;
//        this.personalProjectService = personalProjectService;
//        this.dataStorage = dataStorage;
//    }
//
//    /**
//     * Generates personal project if does NOT exists
//     *
//     * @param user Owner of personal project
//     * @return Created project name
//     */
//    protected String generatePersonalProject(User user) {
//        Optional<String> projectName = projectRepository.findPersonalProjectName(user.getLogin());
//        return projectName.orElseGet(() -> {
//            Project personalProject = personalProjectService.generatePersonalProject(user);
//            projectRepository.save(personalProject);
//            return personalProject.getId();
//        });
//    }

//    /**
//     * Generates default metainfo
//     *
//     * @return Default meta info
//     */
//    protected User.MetaInfo defaultMetaInfo() {
//        User.MetaInfo metaInfo = new User.MetaInfo();
//        Date now = Date.from(ZonedDateTime.now().toInstant());
//        metaInfo.setLastLogin(now);
//        metaInfo.setSynchronizationDate(now);
//        return metaInfo;
//    }

//    /**
//     * Checks email is available
//     *
//     * @param email email to check
//     */
//    protected void checkEmail(String email) {
//        if (userRepository
//                .exists(Filter.builder().withTarget(User.class).withCondition(builder().eq("email", email).build())
//                        .build())) {
//            throw new UserSynchronizationException("User with email '" + email + "' already exists");
//        }
//    }

//    protected String uploadPhoto(String login, byte[] data) {
//        return uploadPhoto(login, data, resolveContentType(data));
//    }
//
//    protected String uploadPhoto(String login, byte[] data, String contentType) {
//        BinaryData photo = new BinaryData(contentType, (long) data.length, new ByteArrayInputStream(data));
//        return uploadPhoto(login, photo);
//    }
//
//    protected String uploadPhoto(String login, BinaryData data) {
//        return userRepository.uploadUserPhoto(login, data);
//    }

    private String resolveContentType(byte[] data) {
        AutoDetectParser parser = new AutoDetectParser(new ImageParser());
        try {
            return parser.getDetector().detect(TikaInputStream.get(data), new Metadata()).toString();
        } catch (IOException e) {
            return MediaType.OCTET_STREAM.toString();
        }
    }
}
