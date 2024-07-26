package org.minuteflow.tstapp.jpa;

/*-
 * ========================LICENSE_START=================================
 * minuteflow-test-application
 * %%
 * Copyright (C) 2024 Jan Komrska
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.minuteflow.core.MinuteFlowConfiguration;
import org.minuteflow.core.api.annotation.ActionRef;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.MinuteEntityRef;
import org.minuteflow.core.api.annotation.MinuteServiceRef;
import org.minuteflow.core.api.bean.BasePropertyState;
import org.minuteflow.core.api.bean.BaseState;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import(MinuteFlowConfiguration.class)
@MinuteServiceRef(serviceClass = FileManager.class, staticState = "fileStateAny")
@MinuteEntityRef(entityClass = FileEntity.class, statePattern = { "fileState*" }, //
        repositoryClass = FileEntityRepository.class, defaultFindMethod = "findById")
public class FileConfiguration {
    @Bean
    public State fileStateAny() {
        return new BaseState();
    }

    @Bean
    public State fileStatePdf() {
        return new BasePropertyState(fileStateAny()).withProperty("type", FileEntityType.PDF);
    }

    @Bean
    public State fileStateHtml() {
        return new BasePropertyState(fileStateAny()).withProperty("type", FileEntityType.HTML);
    }

    //

    @ControllerRef("fileStateAny")
    @Bean
    public FileManager fileManagerStateAny() {
        return new FileManager() {
            @Autowired
            private FileEntityRepository fileEntityRepository;

            @ActionRef
            @Override
            public Source<FileEntity> create(String name, FileEntityType type) {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setName(name);
                fileEntity.setType(type);
                //
                fileEntity = fileEntityRepository.save(fileEntity);
                log.info("created file: " + fileEntity);
                return Source.withParameters(fileEntity.getId());
            }

            @ActionRef
            @Override
            public void delete(Source<FileEntity> fileEntity) {
                fileEntity.markForDelete();
                log.info("deleted file: " + fileEntity);
            }
        };
    }

    @ControllerRef("fileStatePdf")
    @Bean
    public FileManager fileManagerStatePdf() {
        return new FileManager() {
            @ActionRef
            @Override
            public void print(Source<FileEntity> fileEntity) {
                log.info("printing PDF file: " + fileEntity);
            }
        };
    }

    @ControllerRef("fileStateHtml")
    @Bean
    public FileManager fileManagerStateHtml() {
        return new FileManager() {
            @ActionRef
            @Override
            public void print(Source<FileEntity> fileEntity) {
                log.info("printing HTML file: " + fileEntity);
            }
        };
    }
}
