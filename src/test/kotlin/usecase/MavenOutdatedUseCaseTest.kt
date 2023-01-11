package usecase

import domain.*
import io.mockk.*
import org.junit.jupiter.api.Test

class MavenOutdatedUseCaseTest {

    @Test
    fun `依存情報を取得し、更新がされていないアーティファクトが存在すれば出力する`() {
        val mavenRemoteRepositoryPort = mockk<MavenRemoteRepositoryPort>()
        val outdatedArtifactOutputPort = mockk<OutdatedArtifactOutputPort>()
        val useCase = MavenOutdatedUseCase(mavenRemoteRepositoryPort, outdatedArtifactOutputPort)

        val artifact1 = Artifact("org.apache.maven", "maven-core")
        val artifact2 = Artifact("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        val artifacts = listOf(artifact1, artifact2)
        val remoteRepository1 = RemoteRepository("central", "https://repo1.maven.org/maven2/")
        val remoteRepository2 = RemoteRepository("google-asia", "https://maven-central-asia.storage-download.googleapis.com/")
        val remoteRepositories = listOf(remoteRepository1, remoteRepository2)
        val remoteArtifactCandidate1 = RemoteArtifactCandidate(artifact1, remoteRepositories)
        val remoteArtifactCandidate2 = RemoteArtifactCandidate(artifact2, remoteRepositories)
        val latestRemoteArtifact1 = mockk<LatestRemoteArtifact>()
        val latestRemoteArtifact2 = mockk<LatestRemoteArtifact>()
        val result1 = Found(latestRemoteArtifact1)
        val result2 = Found(latestRemoteArtifact2)
        val outdatedArtifacts = listOf(
            latestRemoteArtifact1
        )

        val thresholdYear = 1L

        every { mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate1, takeOutLastUpdated()) } returns result1
        every { mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate2, takeOutLastUpdated()) } returns result2
        every { latestRemoteArtifact1.isOutdated(thresholdYear) } returns true
        every { latestRemoteArtifact2.isOutdated(thresholdYear) } returns false
        every { outdatedArtifactOutputPort.output(outdatedArtifacts) } just Runs

        useCase.verifyArtifacts(artifacts, remoteRepositories, thresholdYear)

        verify {
            mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate1, takeOutLastUpdated())
            mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate2, takeOutLastUpdated())
            outdatedArtifactOutputPort.output(outdatedArtifacts)
        }
    }

    @Test
    fun `依存情報を取得し、最新のアーティファクトを取得できたものだけ有効範囲内か確認する`() {
        val mavenRemoteRepositoryPort = mockk<MavenRemoteRepositoryPort>()
        val outdatedArtifactOutputPort = mockk<OutdatedArtifactOutputPort>()
        val useCase = MavenOutdatedUseCase(mavenRemoteRepositoryPort, outdatedArtifactOutputPort)

        val artifact1 = Artifact("org.apache.maven", "maven-core")
        val artifact2 = Artifact("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        val artifacts = listOf(artifact1, artifact2)
        val remoteRepository1 = RemoteRepository("central", "https://repo1.maven.org/maven2/")
        val remoteRepository2 = RemoteRepository("google-asia", "https://maven-central-asia.storage-download.googleapis.com/")
        val remoteRepositories = listOf(remoteRepository1, remoteRepository2)
        val remoteArtifactCandidate1 = RemoteArtifactCandidate(artifact1, remoteRepositories)
        val remoteArtifactCandidate2 = RemoteArtifactCandidate(artifact2, remoteRepositories)
        val latestRemoteArtifact1 = mockk<LatestRemoteArtifact>()
        val result1 = Found(latestRemoteArtifact1)
        val result2 = NotFound(remoteArtifactCandidate2)
        val outdatedArtifacts = listOf(
            latestRemoteArtifact1
        )

        val thresholdYear = 1L

        every { mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate1, takeOutLastUpdated()) } returns result1
        every { mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate2, takeOutLastUpdated()) } returns result2
        every { latestRemoteArtifact1.isOutdated(thresholdYear) } returns true
        every { outdatedArtifactOutputPort.output(outdatedArtifacts) } just Runs

        useCase.verifyArtifacts(artifacts, remoteRepositories, thresholdYear)

        verify {
            mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate1, takeOutLastUpdated())
            mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate2, takeOutLastUpdated())
            outdatedArtifactOutputPort.output(outdatedArtifacts)
        }
    }

    @Test
    fun `依存情報を取得し、更新がされていないアーティファクトが存在しなければ何もしない`() {
        val mavenRemoteRepositoryPort = mockk<MavenRemoteRepositoryPort>()
        val outdatedArtifactOutputPort = mockk<OutdatedArtifactOutputPort>()
        val useCase = MavenOutdatedUseCase(mavenRemoteRepositoryPort, outdatedArtifactOutputPort)

        val artifact = Artifact("org.apache.maven", "maven-core")
        val artifacts = listOf(artifact)
        val remoteRepository1 = RemoteRepository("central", "https://repo1.maven.org/maven2/")
        val remoteRepository2 = RemoteRepository("google-asia", "https://maven-central-asia.storage-download.googleapis.com/")
        val remoteRepositories = listOf(remoteRepository1, remoteRepository2)
        val remoteArtifactCandidate = RemoteArtifactCandidate(artifact, remoteRepositories)
        val latestRemoteArtifact = mockk<LatestRemoteArtifact>()
        val result1 = Found(latestRemoteArtifact)
        val outdatedArtifacts = emptyList<LatestRemoteArtifact>()

        val thresholdYear = 1L

        every { mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate, takeOutLastUpdated()) } returns result1
        every { latestRemoteArtifact.isOutdated(thresholdYear) } returns false

        useCase.verifyArtifacts(artifacts, remoteRepositories, thresholdYear)

        verify {
            mavenRemoteRepositoryPort.fetchLatestRemoteArtifact(remoteArtifactCandidate, takeOutLastUpdated())
        }
        verify(exactly = 0) {
            outdatedArtifactOutputPort.output(outdatedArtifacts)
        }
    }
}