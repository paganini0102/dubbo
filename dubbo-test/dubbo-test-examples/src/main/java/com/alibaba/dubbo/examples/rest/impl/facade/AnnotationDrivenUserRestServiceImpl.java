/**
 * Copyright 1999-2014 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.examples.rest.impl.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.examples.rest.api.User;
import com.alibaba.dubbo.examples.rest.api.UserService;
import com.alibaba.dubbo.examples.rest.api.facade.RegistrationResult;
import com.alibaba.dubbo.examples.rest.api.facade.UserRestService;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author lishen
 */
@Service(protocol = {"rest", "dubbo"}, group = "annotationConfig", validation = "true")
@Path("customers")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public class AnnotationDrivenUserRestServiceImpl implements UserRestService {

//    private static final Logger logger = LoggerFactory.getLogger(UserRestServiceImpl.class);

    @Autowired
    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("{id : \\d+}")
    public User getUser(@PathParam("id") Long id/*, @Context HttpServletRequest request*/) {
        // test context injection
//        System.out.println("Client address from @Context injection: " + (request != null ? request.getRemoteAddr() : ""));
//        System.out.println("Client address from RpcContext: " + RpcContext.getContext().getRemoteAddressString());
        return userService.getUser(id);
    }

    @POST
    @Path("register")
    public RegistrationResult registerUser(User user) {
        return new RegistrationResult(userService.registerUser(user));
    }
}
