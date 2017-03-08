import { PopulateProject } from "@atomist/rug/operations/ProjectGenerator";
import { Generator, Tags, Parameter } from "@atomist/rug/operations/Decorators";
import { Pattern } from "@atomist/rug/operations/RugOperation";
import { Project } from "@atomist/rug/model/Project";
import {Pom} from "@atomist/rug/model/Pom"
import { PathExpressionEngine } from "@atomist/rug/tree/PathExpression";

@Generator("NewScalaRugFunctionProject", "Creates a new Rug Function project in Scala")
@Tags("rug", "function", "scala")
class NewScalaRugFunctionProject implements PopulateProject {

    @Parameter({description: "Name of the new project", pattern: Pattern.project_name})
    project_name: string

    @Parameter({description: "Name of the first Rug Function", pattern: Pattern.project_name})
    functionName: string

    @Parameter({description: "GroupId to use in the pom. Also used for package", pattern: Pattern.group_id})
    group: string

    populate(project: Project) {
        let eng: PathExpressionEngine = project.context().pathExpressionEngine()
        eng.with<Pom>(project, "/*[@name='pom.xml']/Pom()", pom => {
          pom.removeDependency("com.atomist", "github-lib")
          pom.removeDependency("org.kohsuke", "github-api")
          pom.setArtifactId(this.project_name)
          pom.setGroupId(this.group)
          pom.setDescription(this.project_name)
          pom.setProjectName(this.project_name)
          pom.setVersion("0.1.0-SNAPSHOT")
        })
        project.editWith("AddNewRugFunction", this)
    }
}
export const newRugFunction = new NewScalaRugFunctionProject();
