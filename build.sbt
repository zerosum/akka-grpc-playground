lazy val server = (project in file("server"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(protobufSettings)

lazy val root = (project in file("."))
  .aggregate(server)

lazy val protobufSettings = Seq(
  Compile / PB.protoSources ++= Seq(
    (LocalRootProject / baseDirectory).value / "protobuf" / "public" / "api",
    (LocalRootProject / baseDirectory).value / "protobuf" / "private"
  )
)
