import {Component, OnInit} from '@angular/core';
import {WebSocketService} from './services/web-socket.service';
import {filter, map} from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  eventStatus = {
    AddExample: false,
    CopyPaste: false,
    AddSearch: false
  };

  public constructor(private webSocketService: WebSocketService) {
  }

  sendEvent(eventName: string) {
    this.eventStatus[eventName] = false;

    const event = {
      event_name: eventName,
      user: 'demo',
      event_reference: 'mongo-db',
      event_time: new Date().toISOString(),
      processed: false
    };

    const message = 'INSERT\n{}\n' + JSON.stringify(event);
    this.webSocketService.send(message);
    console.log(message);
  }

  ngOnInit(): void {
    this.webSocketService.incoming$.subscribe(d => console.log(d));

    this.webSocketService.incoming$.pipe(
      map(d => d.split('\n')),
      filter(l => l[0] === 'MESSAGE'),
      map(l => l[2]),
      map(d => JSON.parse(d))
    ).subscribe(d => this.eventStatus[d.event_name] = d.processed);
  }
}
