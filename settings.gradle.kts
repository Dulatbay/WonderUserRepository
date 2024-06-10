rootProject.name = "WonderUserRepository"
include("client-libs:kaspi")
findProject(":client-libs:kaspi")?.name = "kaspi"
include("client-libs:wonder-file-manager")
findProject(":client-libs:wonder-file-manager")?.name = "wonder-file-manager"
