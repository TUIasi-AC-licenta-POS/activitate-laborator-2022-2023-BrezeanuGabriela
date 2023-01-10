import React from 'react';
import Home from "./Home";
import Button from 'react-bootstrap/Button';

class Viewsongs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: [],
            isLoaded: false,
            buttonPressed: "",
            noPage: 0,
            errorMessage: ""
        };
        console.log(this.props);
        this.setPageNo = this.setPageNo.bind(this);
        this.makeRestReq = this.makeRestReq.bind(this);
    }

    setViewMusicDestination(message) {
        this.setState({ buttonPressed: message });
    }

    setPageNo(message) {
        if (message === "next") {
            // console.log(this.state.noPage);
            let noPage = this.state.noPage + 1;
            // console.log(noPage);
            this.setState({ noPage: noPage });
            // this.makeRestReq();
        }
        else if (message === "prev") {
            let noPage = this.state.noPage - 1;
            // console.log(this.state.noPage);
            if (noPage < 0)
                alert("Sunteti deja pe prima pragina!");
            else {
                // console.log(noPage);
                this.setState({ noPage: noPage });
                // this.makeRestReq();
            }
        }
    }

    getSnapshotBeforeUpdate(prevProps, prevState) {
        return prevState;
      }

    componentDidUpdate(prevProps, prevState)
    {
        if(prevState.noPage != this.state.noPage)
            this.makeRestReq();
    }

    makeRestReq() {
        // console.log("lalala   " + this.state.noPage);
        fetch(
            "http://localhost:8080/api/songcollection/songs/?page=" + this.state.noPage,
            {
                method: 'get',
                mode: 'cors',
                headers: {
                    'Access-Control-Allow-Origin': '*'
                }
            })
            .then((res) => {
                return res.json()
            })
            .then((json) => {
                // console.log(json);

                // verificam ca am primit songs
                if (json._embedded !== undefined) {
                    let _embedded = json._embedded;
                    let musics = _embedded.musics;
                    let _links = json._links;
                    // console.log(_embedded.musics);

                    // console.log(_links);
                    this.setState({
                        items: musics,
                        isLoaded: true
                    });
                }
                else {
                    alert("Nu mai sunt piese disponibile!");
                }
            })
            .catch((error) => {
                console.log(error);
                alert("Daca vrei sa mearga, porneste si Rest Sportify(SQL)!");
            });
    }

    componentDidMount() {
        this.makeRestReq();
    }

    render() {
        if (this.state.buttonPressed === "home") {
            return (
                <Home token={this.props.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        if (!this.state.isLoaded) {
            return (
                <>
                    <p> Asteptati sa se incarce piesele. </p>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </>
            )
        }
        else {
            return (
                <div className="App" >

                    <h1 > Music from Rest </h1>
                    <table >
                        <tr >
                            <th> Name </th>
                            <th> Genre</th>
                            <th> Type</th>
                            <th> Year</th>
                        </tr >
                        {

                            this.state.items.map((item) => (


                                <tr>
                                    <td > {item.name} </td>
                                    <td > {item.genre} </td>
                                    <td > {item.type} </td>
                                    <td > {item.year} </td>
                                </tr>
                            ))
                        }
                    </table>
                    <br></br>
                    <p>Pagina {this.state.noPage}</p>
                    <br></br>
                    <Button className="button" onClick={(e) => this.setPageNo("prev")}>Previous</Button>
                    <Button className="button" onClick={(e) => this.setPageNo("next")}>Next</Button>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </div >
            );
        }
    };
}

export default Viewsongs;