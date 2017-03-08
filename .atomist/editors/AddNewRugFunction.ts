import { EditProject } from "@atomist/rug/operations/ProjectEditor";
import { Editor, Tags, Parameter } from "@atomist/rug/operations/Decorators";
import { Pattern } from "@atomist/rug/operations/RugOperation";
import { Project } from "@atomist/rug/model/Project";
import { ScalaFile } from "@atomist/rug/model/ScalaFile"
import { PathExpressionEngine } from "@atomist/rug/tree/PathExpression";

@Editor("AddNewRugFunction", "Add a new Scala Rug Function to a Scala Rug Function Project")
@Tags("rug", "function", "scala")
class AddNewRugFunction implements EditProject {

    @Parameter({description: "Name of the new Rug Function", pattern: "^\\w+$"})
    functionName: string

    @Parameter({description: "GroupId to use in the pom. Also used for package", pattern: Pattern.group_id})
    group: string

    @Parameter({description: "ArtifactId to use in the pom. Also used for package", pattern: Pattern.group_id})
    project_name: string

    edit(project: Project) {
        let eng: PathExpressionEngine = project.context().pathExpressionEngine()
        let outputDir = `src/main/scala/${this.group.replace(/\./g, "/")}`
        project.mergeTemplates("",outputDir,{})
        let fnFile = `${outputDir}/${this.functionName}.scala`
        project.copyFile(`${outputDir}/RugFunction.scala`,fnFile)
        project.deleteFile(`${outputDir}/RugFunction.scala`)
        project.replace("__CLASSNAME__", this.functionName)
        project.replace("__PACKAGE_NAME__", `${this.group}`)

    }
}
export const newRugFunction = new AddNewRugFunction();
