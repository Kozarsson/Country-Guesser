# Project Report

## A CI/CD Pipeline for signing an APK and publishing it to GitHub Releases

## The CI/CD architecture

Creating Android APKs manually can be a slow process, and includes numerous steps, especially if signing is involved. This pipeline does this, with build, packaging, versioning and publication. The project's architecture uses a CI/CD pipeline with GitHub Actions, in order to build quality and security into the flow so that bugs aren't noticed too late. Changes pushed to the DD2482 branch trigger automated checks consisting of linting, unit tests, and debug build. These are key choices because they automate repeatable quality checks during development. This follows the principle of detecting problems automatically during the development itself, rather than relying on manual testing after the development.

It is essential that small, frequent changes and releases are being used. Ideally, the code should always be ready to be automatically released. The pipeline separated the CI validation from the release of a new version. In this project, releases are automatically triggered when a version tag matching `v*` is pushed. Developers can therefore make smaller changes and receive automatic feedback from the CI pipeline, while releases can be handled through version tags. When changes are smaller and more frequent, problems can be identified closer to the point where they are introduced.

The project pipeline's components form a sequence with sections that are automated. Automation is often more efficient in terms of personnel and time, etc. The parts that, in particular should be automated, are the parts that benefit from repeatability. When a push or a pull request satisfies the pipeline flow's conditions, Github actions checks out the repository. The pipeline then executes the project's quality checks. The CI/CD Pipeline (including Docker) takes around 7 minutes to complete.

## Automated build and testing

### CI Pipeline

The pipeline can be overseen in YAML. The build job runs on pushes and pull requests to DD2482, and also on `v*` tags. Only the release job is tag-only. GitHub actions will trigger and automatically run lints, tests, and a build version. The CI is also where Docker is the core, but we will talk more about this when it comes to the infrastructure.

## Automated Deployment/Delivery

### CD Pipeline

This is the release job in the YAML runs only when you tag a version using `v*` (e.g. `v1.2.3`) builds the real, signed APK file and publishes it on GitHub Release, providing a ready-to-download artefact. With packaging, versioning and publication being done automatically in one step at the end in the CD pipeline. The build and release jobs are triggered by the same tag push.

### The CI/CD Pipeline

Push ➔ Tests ➔ Build/Sign ➔ Release ➔ Upload/Publish.

## Infrastructure configuration (IaC)

Docker is used as the core of the CI. The project uses a Dockerfile to define the environment used for the CI build. The Docker image is based on Eclipse Temurin with Java 17 and installs the Android command-line tools, Android platform tools, Android API 35 and build-tool. The CI build runs inside a Docker container defined by the repository's Dockerfile, which copies the project into the Docker image, makes the Gradle wrapper executable and sets the Gradle command that runs when the container starts. Defining the build environment as code, rather than relying on the configuration of the GitHub Actions runner, improves reproducibility. This is helpful for collaboration between different groups of developers.

Additionally to Docker, the IaC also uses the Terraform tool. Terraform is used to define and manage the Google Cloud infrastructure supporting the Country Guesser application. The Terraform configuration specifies the Firebase project and enables the Google Cloud APIs required by Firestore and Cloud Functions. By storing this infrastructure configuration in version control, the project infrastructure can be reviewed, validated, and reproduced consistently across development and deployment environments. For example, an external developer wanting to make their own version of the application would simply create a new Google Cloud project and then update `project_id` in `terraform.tfvars` to a new id of their own. Terraform separates infrastructure management from application code while providing a controlled workflow through commands such as `terraform plan` and `terraform apply`, reducing manual configuration and improving the reliability of the DevOps process.

So, Docker makes the build environment reproducible, while Terraform makes the cloud infrastructure reproducible. Both environments are defined, Docker on build and Terraform on backend. Because these definitions are stored in version control, different groups of developers can set up the same environment without manual configuration. So Docker and Terraform make up the IaC of this project.

## Limitations and trade-offs

If the repo's automatic test suite doesn't have enough coverage and important bugs are not discovered, they will quickly reach the users. For a small project or infrequent releases a CI/CD pipeline will be unfitting and more costly than the time and effort saved by automating the build, testing, and deployment etc. For a project like this, where not many collaborators are involved, a CI/CD pipeline may not offer many benefits.

The CI/CD pipeline is typically high maintenance, including its initial setup. Pipeline maintenance such as keys and dependencies may quickly become outdated. This raises the standards for regular quality and security checks, caching etc. A limitation is that this pipeline uses no caching, which could save time. Continuous deployment in particular may require particular monitoring and rollback procedures. The pipeline took around 7 minutes to complete.

## Quality or security automation

The pipeline uses `./gradlew lint`, lint is Android Studio's in-build code scanning tool (static code analysis). The lint tool automatically checks the Android project source for bugs and security and more. The pipeline also uses `./gradlew test` for automatically running tests.

## Documented use of AI-assisted tools

Generative AI was used according to this course's AI policy. AI was used as a support tool for understanding GitHub Actions, Docker, Gradle and CI/CD configuration, and for troubleshooting in the CI/CD pipeline. Every step in the CI/CD pipeline was studied and tested. Everything was pushed and controlled and corrected in GitHub. The singular unit test used for the CI pipeline was fully AI-generated, since the core of this course is not about manually writing tests but the automation of them.
