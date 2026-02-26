import { Component, OnDestroy, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { VersionInfo } from "app/models/version-info";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";

@Component({
  selector: "app-about",
  templateUrl: "./about.component.html",
  standalone: false,
})
export class AboutComponent
  extends BaseDialogComponent
  implements OnInit, OnDestroy
{
  public versionInfo: VersionInfo[] = [];
  public mostRecentApplicationVersion = "";
  public currentDate: Date = new Date();
  public loading = true;
  public databaseName = "";
  public databaseUrl = "";
  public tableTitles = [
    "Services",
    "Capacity Statement",
    "Interface",
    "IE-MDM API-Server",
  ];

  private versionInfoSubscription!: Subscription;

  public ngOnInit(): void {
    this.getIEMDMVersionInfo();
  }

  public ngOnDestroy(): void {
    if (this.versionInfoSubscription != null) {
      this.versionInfoSubscription.unsubscribe();
    }
  }

  public copyMessage(): void {
    navigator.clipboard.writeText(this.generateDebugMessage());
  }

  private getIEMDMVersionInfo(): void {
    this.versionInfoSubscription = this.dataService
      .getIEMDMVersionInfo()
      .subscribe((res) => {
        this.versionInfo = res;
        this.loading = false;

        this.databaseName = this.versionInfo[0].databaseName;
        this.databaseUrl = this.versionInfo[0].databaseUrl;
        this.mostRecentApplicationVersion = this.versionInfo[0].version;
      });
  }

  private generateDebugMessage(): string {
    const url = window.location.href;
    let environment = "PROD";

    if (url.indexOf("localhost") > -1) {
      environment = "LH";
    } else if (url.indexOf(".dev1") > -1) {
      environment = "DEV1";
    } else if (url.indexOf(".dev2") > -1) {
      environment = "DEV2";
    } else if (url.indexOf(".dev") > -1) {
      environment = "DEV";
    } else if (url.indexOf(".qa1") > -1) {
      environment = "QA1";
    } else if (url.indexOf(".qa") > -1) {
      environment = "QA";
    }

    let debugMessage = `Environment: ${environment}\nDatabase: ${this.databaseName} (${this.databaseUrl})\n\n`;

    this.versionInfo.forEach((element) => {
      const name = "name: " + element.name + "\n";
      const buildDate = "build date: " + element.buildDate + "\n";
      const commitHashShort = "commit: " + element.commitHashShort + "\n";
      const version = "version: " + element.version + "\n";
      const node = "node: " + element.node + "\n\n";
      debugMessage = debugMessage.concat(
        name,
        buildDate,
        commitHashShort,
        version,
        node
      );
    });

    return debugMessage.toString();
  }
}
