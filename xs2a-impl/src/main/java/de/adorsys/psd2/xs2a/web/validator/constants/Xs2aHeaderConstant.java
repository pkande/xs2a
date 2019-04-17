/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.validator.constants;

public class Xs2aHeaderConstant {
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String PSU_IP_ADDRESS = "PSU-IP-Address";

    public static final String PSU_ID = "PSU-ID";
    public static final String PSU_ID_TYPE = "PSU-ID-Type";
    public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
    public static final String PSU_CORPORATE_ID_TYPE = "PSU-Corporate-ID-Type";
    public static final String AUTHORISATION = "Authorization";

    public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
    public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";

    public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
    public static final String TPP_EXPLICIT_AUTHORISATION_PREFERRED = "TPP-Explicit-Authorisation-Preferred";

    private Xs2aHeaderConstant() {
    }
}
