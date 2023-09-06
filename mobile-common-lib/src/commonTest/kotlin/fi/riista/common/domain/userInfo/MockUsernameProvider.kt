package fi.riista.common.domain.userInfo

import fi.riista.common.domain.dto.MockUserInfo

class MockUsernameProvider(
    override var username: String = MockUserInfo.PenttiUsername
): UsernameProvider
