package com.devspace.environment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class GitHubRepositoryClient {

    private final RestClient restClient;

    public GitHubRepositoryClient() {

        this.restClient =
                RestClient.builder()
                        .baseUrl("https://api.github.com")
                        .defaultHeader(
                                "Accept",
                                "application/vnd.github+json"
                        )
                        .build();
    }

    public void validateRepositoryAndBranch(
            String repositoryUrl,
            String branchName) {

        GitHubRepositoryInfo repositoryInfo =
                extractRepositoryInfo(
                        repositoryUrl
                );

        try {

            restClient.get()
                    .uri(
                            "/repos/{owner}/{repository}/branches/{branch}",
                            repositoryInfo.owner(),
                            repositoryInfo.repository(),
                            branchName
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound ex) {

            throw new IllegalArgumentException(
                    "GitHub repository or branch not found: "
                            + repositoryUrl
                            + " [branch="
                            + branchName
                            + "]"
            );

        } catch (HttpClientErrorException ex) {

            throw new IllegalArgumentException(
                    "Unable to validate GitHub repository: "
                            + ex.getStatusCode()
            );
        }
    }

    private GitHubRepositoryInfo extractRepositoryInfo(
            String repositoryUrl) {

        if (repositoryUrl == null
                || repositoryUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Repository URL is required"
            );
        }

        if (!repositoryUrl.startsWith(
                "https://github.com/")) {

            throw new IllegalArgumentException(
                    "Only GitHub repositories are currently supported"
            );
        }

        String path =
                repositoryUrl.replace(
                        "https://github.com/",
                        ""
                );

        if (path.endsWith(".git")) {

            path =
                    path.substring(
                            0,
                            path.length() - 4
                    );
        }

        String[] parts =
                path.split("/");

        if (parts.length != 2
                || parts[0].isBlank()
                || parts[1].isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL"
            );
        }

        return new GitHubRepositoryInfo(
                parts[0],
                parts[1]
        );
    }

    private record GitHubRepositoryInfo(
            String owner,
            String repository) {
    }
}