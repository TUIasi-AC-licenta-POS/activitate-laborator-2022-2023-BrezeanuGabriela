import React from 'react';
import Viewsongs from './ViewSongs';
// import { Link, Route, Routes, BrowserRouter, Router , Switch} from "react-router-dom";
import Button from 'react-bootstrap/Button';
import './index.css';
import AddArtist from './AddArtist';
import Login from './Login';
import Viewartists from './ViewArtists';
import AddMusic from './AddMusic';
import AddProfile from './AddProfile';
import Viewplaylists from './ViewPlaylists';
import AddPlaylist from './AddPlaylist';

class Home extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loginToken: this.props.token,
            buttonPressed: "",
            roles: []
        };
        console.log(props);
        console.log(this.state);
    }

    logout(token) {
        let xmlHttp = new XMLHttpRequest();
        xmlHttp.open("POST", "http://127.0.0.1:8000", true);

        let message =
            "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
            "<soap11env:Body>" +
            "<sample:Logout>" +
            "<sample:token>" + token + "</sample:token>" +
            "</sample:Logout>" +
            "</soap11env:Body>" +
            "</soap11env:Envelope>";

        
        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState === 4) {
                if (xmlHttp.status === 200) {
                    alert("Veti fi redirectat catre Login");
                }
            }
        }
        xmlHttp.send(message);
    }

    makeSoapRequest(token) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", "http://127.0.0.1:8000", true);

            let message =
                "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
                "<soap11env:Body>" +
                "<sample:Authorize>" +
                "<sample:token>" + token + "</sample:token>" +
                "</sample:Authorize>" +
                "</soap11env:Body>" +
                "</soap11env:Envelope>";

            let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 200) {
                        var XMLParser = require('react-xml-parser');
                        var xml = new XMLParser().parseFromString(xmlHttp.responseText);
                        let response = xml.children[0].children[0].children[0];
                        responseMessage = response.value;
                        // nu e vreo eroare
                        if (responseMessage.split("Error:").length === 1) {
                            resolve(responseMessage);
                        }
                        else {                            
                            reject(responseMessage.split("Error:")[1]);
                        }
                    }
                    else {
                        console.log(xmlHttp.status);
                        console.log(xmlHttp.responseText);
                        alert("Reminder: Porneste serviciul SOAP! (+ server-ul sql)");
                    }
                }
            }
            xmlHttp.send(message);
        });
    }

    async requestLoginToken() {
        this.makeSoapRequest(this.state.loginToken)
            .then(
                (res) => {
                    console.log(res);
                    let token = res;
                    let base64Payload = token.split(".")[1];
                    let payload = atob(base64Payload);
                    payload = JSON.parse(payload.toString());
                    let roles = payload.roles;
                    console.log(roles);
                    this.setState( {roles: roles});
                }
            ).catch( err => {
                this.setState({errorMesage : err});
            });
    }

    componentDidMount() {
        this.requestLoginToken();
    }

    
    setViewMusicDestination(message)
    {   
        this.setState({buttonPressed: message});
    }

    logoutCbk()
    {
        this.logout(this.state.loginToken);
        this.setViewMusicDestination("logout");
    }

    render() {
        // console.log(this.state.buttonPressed);
        if(this.state.buttonPressed === "") 
        {
            if((this.state.roles.length === 1 && this.state.roles[0] === "client") ||
                !this.state.roles.includes("content_manager")) {
                return (
                    <div>
                        <h3> Home </h3>
                        <Button className="button" title="View music" onClick={(e) => this.setViewMusicDestination("viewMusic")}>View Music</Button>
                        <br></br>
                        <Button className="button" title="View artists" onClick={(e) => this.setViewMusicDestination("viewArtists")}>View Artists</Button>
                        <br></br>
                        <Button className="button" title="Add profile" onClick={(e) => this.setViewMusicDestination("addProfile")}>Add profile</Button>
                        <br></br>
                        <Button className="button" title="Add playlist" onClick={(e) => this.setViewMusicDestination("addPlaylist")}>Add playlist</Button>
                        <br></br>
                        <Button className="button" title="View playlists" onClick={(e) => this.setViewMusicDestination("viewPlaylists")}>View playlists</Button>
                        <br></br>
                        <Button className="button" onClick={(e) => this.logoutCbk()}>Log Out</Button>
                    </div>
                    );
            }

            return (
            <div>
                <h3> Home </h3>
                <Button className="button" title="View music" onClick={(e) => this.setViewMusicDestination("viewMusic")}>View Music</Button>
                <br></br>
                <Button className="button" title="Add an artist" onClick={(e) => this.setViewMusicDestination("addAnArtist")}>Add/Update an artist</Button>
                <br></br>
                <Button className="button" title="View artists" onClick={(e) => this.setViewMusicDestination("viewArtists")}>View Artists</Button>
                <br></br>
                <Button className="button" title="Add song/album" onClick={(e) => this.setViewMusicDestination("addMusic")}>Add music</Button>
                <br></br>
                <Button className="button" title="Add profile" onClick={(e) => this.setViewMusicDestination("addProfile")}>Add profile</Button>
                <br></br>
                <Button className="button" title="Add playlist" onClick={(e) => this.setViewMusicDestination("addPlaylist")}>Add playlist</Button>
                <br></br>
                <Button className="button" title="View playlists" onClick={(e) => this.setViewMusicDestination("viewPlaylists")}>View playlists</Button>
                <br></br>
                <Button className="button" onClick={(e) => this.logoutCbk()}>Log Out</Button>
            </div>
            );
        }
        else if(this.state.buttonPressed === "viewMusic")
        {
            return (
                <Viewsongs token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "addAnArtist")
        {
            return (
                <AddArtist token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "addMusic")
        {
            return (
                <AddMusic token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "addProfile")
        {
            return (
                <AddProfile token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "addPlaylist")
        {
            return (
                <AddPlaylist token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "viewArtists")
        {
            return (
                <Viewartists token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "viewPlaylists")
        {
            return (
                <Viewplaylists token={this.state.loginToken}
                            username={this.props.username}
                            password={this.props.password} 
                            id={this.props.id}/>
            );
        }
        else if(this.state.buttonPressed === "logout")
        {
            return(
                <Login />
            )
        }
    };
}

export default Home;