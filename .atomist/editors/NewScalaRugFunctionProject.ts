import { PopulateProject } from "@atomist/rug/operations/ProjectGenerator";
import { Generator, Tags, Parameter } from "@atomist/rug/operations/Decorators";
import { Pattern } from "@atomist/rug/operations/RugOperation";
import { Project } from "@atomist/rug/model/Project";
import {Pom} from "@atomist/rug/model/Pom"
import { PathExpressionEngine } from "@atomist/rug/tree/PathExpression";

@Generator("NewScalaRugFunctionProject", "Creates a new Rug Function project in Scala")
@Tags("rug", "function", "scala")
class NewScalaRugFunctionProject implements PopulateProject {

    @Parameter({description: "Name of the new project", pattern: Pattern.any})
    project_name: string

    @Parameter({description: "GroupId to use in the pom. Also used for package", pattern: Pattern.group_id})
    group: string

    @Parameter({description: "ArtifactId to use in the pom. Also used for package", pattern: Pattern.group_id})
    artifact: string

    populate(project: Project) {
        let eng: PathExpressionEngine = project.context().pathExpressionEngine()
        eng.with<Pom>(project, "/*[@name='pom.xml']/Pom()", pom => {
          pom.removeDependency("com.atomist", "github-lib")
          pom.removeDependency("org.kohsuke", "github-api")
          pom.setArtifactId(this.artifact)
          pom.setGroupId(this.group)
          pom.setDescription(this.project_name)
          pom.setProjectName(this.project_name)
          pom.setVersion("0.1.0-SNAPSHOT")
        })
        let params: any = Object.create(this)
        params["functionName"] = this.project_name
        project.editWith("AddNewRugFunction", params)
    }
}
export const newRugFunction = new NewScalaRugFunctionProject();
